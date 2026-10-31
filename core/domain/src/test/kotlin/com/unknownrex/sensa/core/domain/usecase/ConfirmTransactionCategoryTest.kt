package com.unknownrex.sensa.core.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.EmptyResult
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.MerchantRule
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.model.TransactionStatus
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.MerchantRuleRepository
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ConfirmTransactionCategoryTest {

    private val transactions = InMemoryTransactionRepository()
    private val categories = InMemoryCategoryRepository()
    private val rules = InMemoryMerchantRuleRepository()
    private val confirm = ConfirmTransactionCategory(
        transactionRepository = transactions,
        merchantRuleRepository = rules,
        categoryRepository = categories,
        clock = { 42L },
    )

    @Test
    fun `confirming marks the row confirmed and learns the merchant`() = runTest {
        transactions.rows += reviewRow(merchant = "Kopi Kenangan Medan")
        categories.ids += "food"

        val result = confirm("tx-1", "food")

        assertThat(result is Result.Success).isTrue()
        val updated = transactions.rows.single()
        assertThat(updated.categoryId).isEqualTo("food")
        assertThat(updated.status).isEqualTo(TransactionStatus.CONFIRMED)
        assertThat(updated.confidence).isEqualTo(1f)

        val rule = rules.rows.single()
        assertThat(rule.normalizedMerchant).isEqualTo("kopi kenangan medan")
        assertThat(rule.categoryId).isEqualTo("food")
        assertThat(rule.isUserLearned).isTrue()
    }

    @Test
    fun `unknown category is rejected without touching the row`() = runTest {
        transactions.rows += reviewRow(merchant = "Kopi Kenangan Medan")
        categories.ids += "food"

        val result = confirm("tx-1", "does-not-exist")

        assertThat(result is Result.Error).isTrue()
        assertThat((result as Result.Error).error).isEqualTo(DataError.Local.NOT_FOUND)
        assertThat(transactions.rows.single().status).isEqualTo(TransactionStatus.NEEDS_REVIEW)
        assertThat(rules.rows).isEqualTo(emptyList<MerchantRule>())
    }

    @Test
    fun `losing the race to a deleted row fails cleanly`() = runTest {
        categories.ids += "food"

        val result = confirm("missing", "food")

        assertThat(result is Result.Error).isTrue()
        assertThat((result as Result.Error).error).isEqualTo(DataError.Local.NOT_FOUND)
    }

    @Test
    fun `learning can be skipped`() = runTest {
        transactions.rows += reviewRow(merchant = "Budi")
        categories.ids += "food"

        val result = confirm("tx-1", "food", learnMerchant = false)

        assertThat(result is Result.Success).isTrue()
        assertThat(rules.rows).isEqualTo(emptyList<MerchantRule>())
        assertThat(transactions.rows.single().categoryId).isNotNull()
    }

    private fun reviewRow(merchant: String) = Transaction(
        id = "tx-1",
        amountRupiah = 25_000,
        merchant = merchant,
        categoryId = null,
        source = PaymentSource.GOPAY,
        timestamp = 1_000L,
        status = TransactionStatus.NEEDS_REVIEW,
        confidence = 0.1f,
        dedupeKey = "key",
        createdAt = 500L,
    )
}

private class InMemoryTransactionRepository : TransactionRepository {
    val rows = mutableListOf<Transaction>()

    override fun observeRecent(limit: Int): Flow<List<Transaction>> = flowOf(rows)
    override fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> = flowOf(rows)
    override fun observeNeedsReview(): Flow<List<Transaction>> = flowOf(rows)
    override fun observeNeedsReviewCount(): Flow<Int> = flowOf(rows.size)
    override fun observeById(id: String): Flow<Transaction?> = flowOf(rows.firstOrNull { it.id == id })

    override suspend fun getById(id: String): Result<Transaction, DataError.Local> {
        val row = rows.firstOrNull { it.id == id }
        return row?.let { Result.Success(it) } ?: Result.Error(DataError.Local.NOT_FOUND)
    }

    override suspend fun add(transaction: Transaction): EmptyResult<DataError.Local> {
        rows += transaction
        return Result.Success(Unit)
    }

    override suspend fun update(transaction: Transaction): EmptyResult<DataError.Local> {
        val index = rows.indexOfFirst { it.id == transaction.id }
        if (index < 0) return Result.Error(DataError.Local.NOT_FOUND)
        rows[index] = transaction
        return Result.Success(Unit)
    }

    override suspend fun delete(id: String): EmptyResult<DataError.Local> {
        rows.removeAll { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun findByDedupeKey(dedupeKey: String): Transaction? = null
}

private class InMemoryCategoryRepository : CategoryRepository {
    val ids = mutableListOf<String>()

    override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())

    override suspend fun getCategory(id: String): Category? =
        ids.firstOrNull { it == id }?.let { id ->
            Category(id = id, name = id, iconKey = id, colorArgb = 0, isDefault = true, sortOrder = 0)
        }

    override suspend fun updateCategory(category: Category): EmptyResult<DataError.Local> =
        Result.Success(Unit)
}

private class InMemoryMerchantRuleRepository : MerchantRuleRepository {
    val rows = mutableListOf<MerchantRule>()

    override fun observeRules(): Flow<List<MerchantRule>> = flowOf(rows)
    override suspend fun findRule(normalizedMerchant: String): MerchantRule? =
        rows.firstOrNull { it.normalizedMerchant == normalizedMerchant }

    override suspend fun upsert(rule: MerchantRule): EmptyResult<DataError.Local> {
        rows.removeAll { it.normalizedMerchant == rule.normalizedMerchant }
        rows += rule
        return Result.Success(Unit)
    }

    override suspend fun delete(id: String): EmptyResult<DataError.Local> {
        rows.removeAll { it.id == id }
        return Result.Success(Unit)
    }
}
