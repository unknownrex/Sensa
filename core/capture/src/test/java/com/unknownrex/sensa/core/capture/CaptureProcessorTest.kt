package com.unknownrex.sensa.core.capture

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.unknownrex.sensa.core.capture.parser.NotificationParserRegistry
import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.EmptyResult
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.capture.NotificationSnapshot
import com.unknownrex.sensa.core.domain.categorization.Categorization
import com.unknownrex.sensa.core.domain.categorization.Categorizer
import com.unknownrex.sensa.core.domain.categorization.SuggestionSource
import com.unknownrex.sensa.core.domain.model.DefaultCategories
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.model.TransactionStatus
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CaptureProcessorTest {

    private val repository = FakeTransactionRepository()
    private val categorizer = FakeCategorizer()
    private val processor = CaptureProcessor(
        parserRegistry = NotificationParserRegistry(),
        categorizer = categorizer,
        transactionRepository = repository,
        clock = { 9_000_000L },
    )

    @Test
    fun `ignores packages without a parser`() = runTest {
        val outcome = processor.process(snapshot(packageName = "com.whatsapp"))

        assertThat(outcome).isEqualTo(CaptureOutcome.Ignored)
        assertThat(repository.rows).isEmpty()
    }

    @Test
    fun `ignores non payment notifications from a supported app`() = runTest {
        val outcome = processor.process(
            snapshot(text = "Promo seru minggu ini, diskon 50% untuk semua merchant!"),
        )

        assertThat(outcome).isEqualTo(CaptureOutcome.Ignored)
    }

    @Test
    fun `auto saves a confident categorization`() = runTest {
        categorizer.result = Categorization("food", 0.9f, SuggestionSource.DEFAULT_RULE)

        val outcome = processor.process(gopayPayment())

        assertThat(outcome is CaptureOutcome.Saved).isTrue()
        val saved = repository.rows.single()
        assertThat(saved.categoryId).isEqualTo("food")
        assertThat(saved.status).isEqualTo(TransactionStatus.AUTO)
        assertThat(saved.source).isEqualTo(PaymentSource.GOPAY)
        assertThat(saved.amountRupiah).isEqualTo(25_000L)
    }

    @Test
    fun `low confidence goes to review`() = runTest {
        categorizer.result = Categorization(DefaultCategories.UNCATEGORIZED_ID, 0.1f, SuggestionSource.FALLBACK)

        val outcome = processor.process(gopayPayment())

        assertThat(outcome is CaptureOutcome.NeedsReview).isTrue()
        assertThat(repository.rows.single().status).isEqualTo(TransactionStatus.NEEDS_REVIEW)
        assertThat(repository.rows.single().categoryId).isEqualTo(null)
    }

    @Test
    fun `transfers always go to review even when the category is confident`() = runTest {
        categorizer.result = Categorization("food", 0.95f, SuggestionSource.DEFAULT_RULE)

        val outcome = processor.process(
            snapshot(text = "Kamu berhasil transfer Rp100.000 ke Warung Kopi Budi"),
        )

        assertThat(outcome is CaptureOutcome.NeedsReview).isTrue()
        assertThat(repository.rows.single().status).isEqualTo(TransactionStatus.NEEDS_REVIEW)
    }

    @Test
    fun `a repeated notification is dropped as duplicate`() = runTest {
        categorizer.result = Categorization("food", 0.9f, SuggestionSource.DEFAULT_RULE)

        processor.process(gopayPayment())
        val second = processor.process(gopayPayment())

        assertThat(second).isEqualTo(CaptureOutcome.Duplicate)
        assertThat(repository.rows).isEqualTo(repository.rows.take(1))
    }

    private fun gopayPayment() = snapshot(
        text = "Pembayaran berhasil Rp25.000 ke Kopi Kenangan",
        postedAt = 1_000_000L,
    )

    private fun snapshot(
        packageName: String = PaymentApps.GOPAY,
        text: String? = null,
        postedAt: Long = 1_000_000L,
    ) = NotificationSnapshot(
        packageName = packageName,
        title = "GoPay",
        text = text,
        bigText = null,
        postedAtMillis = postedAt,
    )
}

private class FakeCategorizer : Categorizer {
    var result: Categorization = Categorization("food", 0.9f, SuggestionSource.DEFAULT_RULE)

    override suspend fun categorize(merchant: String, source: PaymentSource): Categorization = result
}

private class FakeTransactionRepository : TransactionRepository {

    val rows = mutableListOf<Transaction>()

    override fun observeRecent(limit: Int): Flow<List<Transaction>> = flowOf(rows)

    override fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> = flowOf(rows)

    override fun observeNeedsReview(): Flow<List<Transaction>> =
        flowOf(rows.filter { it.status == TransactionStatus.NEEDS_REVIEW })

    override fun observeNeedsReviewCount(): Flow<Int> =
        flowOf(rows.count { it.status == TransactionStatus.NEEDS_REVIEW })

    override fun observeById(id: String): Flow<Transaction?> = flowOf(rows.firstOrNull { it.id == id })

    override suspend fun getById(id: String): Result<Transaction, DataError.Local> {
        val row = rows.firstOrNull { it.id == id }
        return if (row == null) Result.Error(DataError.Local.NOT_FOUND) else Result.Success(row)
    }

    override suspend fun add(transaction: Transaction): EmptyResult<DataError.Local> {
        rows += transaction
        return Result.Success(Unit)
    }

    override suspend fun update(transaction: Transaction): EmptyResult<DataError.Local> {
        val index = rows.indexOfFirst { it.id == transaction.id }
        if (index == -1) return Result.Error(DataError.Local.NOT_FOUND)
        rows[index] = transaction
        return Result.Success(Unit)
    }

    override suspend fun delete(id: String): EmptyResult<DataError.Local> {
        rows.removeAll { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun findByDedupeKey(dedupeKey: String): Transaction? =
        rows.firstOrNull { it.dedupeKey == dedupeKey }
}
