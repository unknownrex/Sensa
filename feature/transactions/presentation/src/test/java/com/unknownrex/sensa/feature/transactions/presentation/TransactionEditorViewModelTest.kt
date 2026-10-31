package com.unknownrex.sensa.feature.transactions.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import androidx.lifecycle.SavedStateHandle
import com.unknownrex.sensa.core.domain.DataError
import com.unknownrex.sensa.core.domain.EmptyResult
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.model.TransactionStatus
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionEditorViewModelTest {

    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `add mode saves a manual transaction with the chosen category`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(TransactionEditorAction.OnAmountChange("35000"))
        viewModel.onAction(TransactionEditorAction.OnMerchantChange("  Kopi Kenangan  "))
        viewModel.onAction(TransactionEditorAction.OnCategorySelected("food"))
        viewModel.onAction(TransactionEditorAction.OnNoteChange("split with Dita"))
        viewModel.onAction(TransactionEditorAction.OnSave)

        val saved = transactions.rows.single()
        assertThat(saved.amountRupiah).isEqualTo(35_000L)
        assertThat(saved.merchant).isEqualTo("Kopi Kenangan")
        assertThat(saved.categoryId).isEqualTo("food")
        assertThat(saved.note).isEqualTo("split with Dita")
        assertThat(saved.source).isEqualTo(PaymentSource.MANUAL)
        assertThat(saved.status).isEqualTo(TransactionStatus.CONFIRMED)
    }

    @Test
    fun `invalid input blocks the save and surfaces field errors`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(TransactionEditorAction.OnSave)

        val state = viewModel.state.value
        assertThat(state.amountError).isNotNull()
        assertThat(state.merchantError).isNotNull()
        assertThat(transactions.rows).isEqualTo(emptyList<Transaction>())
    }

    @Test
    fun `edit mode loads the existing transaction`() = runTest {
        transactions.rows += existing()
        val viewModel = viewModel(transactionId = "tx-1")

        val state = viewModel.state.value
        assertThat(state.mode).isEqualTo(EditorMode.Edit)
        assertThat(state.amountDigits).isEqualTo("25.000")
        assertThat(state.merchant).isEqualTo("Kopi Kenangan")
        assertThat(state.selectedCategoryId).isEqualTo("food")
    }

    @Test
    fun `editing keeps source, timestamp and dedupe key`() = runTest {
        transactions.rows += existing()
        val viewModel = viewModel(transactionId = "tx-1")

        viewModel.onAction(TransactionEditorAction.OnAmountChange("31000"))
        viewModel.onAction(TransactionEditorAction.OnSave)

        val updated = transactions.rows.single()
        assertThat(updated.amountRupiah).isEqualTo(31_000L)
        assertThat(updated.source).isEqualTo(PaymentSource.GOPAY)
        assertThat(updated.timestamp).isEqualTo(1_000L)
        assertThat(updated.dedupeKey).isEqualTo("gopay-key")
    }

    @Test
    fun `delete removes the row and closes the screen`() = runTest {
        transactions.rows += existing()
        val viewModel = viewModel(transactionId = "tx-1")

        viewModel.onAction(TransactionEditorAction.OnDelete)

        assertThat(transactions.rows).isEqualTo(emptyList<Transaction>())
        assertThat(transactions.deletedIds).isEqualTo(listOf("tx-1"))
    }

    @Test
    fun `amount field groups digits as the user types`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(TransactionEditorAction.OnAmountChange("1240000"))

        assertThat(viewModel.state.value.amountDigits).isEqualTo("1.240.000")
    }

    @Test
    fun `negative or empty amount is rejected`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(TransactionEditorAction.OnAmountChange("0"))
        viewModel.onAction(TransactionEditorAction.OnMerchantChange("Kopi"))

        assertThat(viewModel.state.value.amountDigits).isEqualTo("")
        assertThat(viewModel.state.value.amountError).isNull()
    }

    private fun viewModel(transactionId: String? = null): TransactionEditorViewModel {
        val handle = SavedStateHandle(
            buildMap {
                put("transactionId", transactionId)
                transactionId?.let { put("amount", "25.000") }
            },
        )
        return TransactionEditorViewModel(handle, transactions, categories)
    }

    private fun existing() = Transaction(
        id = "tx-1",
        amountRupiah = 25_000,
        merchant = "Kopi Kenangan",
        categoryId = "food",
        source = PaymentSource.GOPAY,
        timestamp = 1_000L,
        note = null,
        status = TransactionStatus.NEEDS_REVIEW,
        confidence = 0.2f,
        dedupeKey = "gopay-key",
        createdAt = 500L,
    )
}

private class FakeTransactionRepository : TransactionRepository {
    val rows = mutableListOf<Transaction>()
    val deletedIds = mutableListOf<String>()

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
        deletedIds += id
        rows.removeAll { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun findByDedupeKey(dedupeKey: String): Transaction? = null
}

private class FakeCategoryRepository : CategoryRepository {
    private val all = listOf(
        Category("food", "Food and drink", "food", 0xFFFFB454, isDefault = true, sortOrder = 0),
        Category("shopping", "Shopping", "shopping", 0xFFFF7A9A, isDefault = true, sortOrder = 1),
    )

    override fun observeCategories(): Flow<List<Category>> = flowOf(all)

    override suspend fun getCategory(id: String): Category? = all.firstOrNull { it.id == id }

    override suspend fun updateCategory(category: Category): EmptyResult<DataError.Local> =
        Result.Success(Unit)
}
