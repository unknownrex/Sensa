package com.unknownrex.sensa.feature.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unknownrex.sensa.core.capture.NotificationAccessChecker
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import com.unknownrex.sensa.core.presentation.model.TransactionGroupUi
import com.unknownrex.sensa.core.presentation.model.groupByDay
import com.unknownrex.sensa.core.presentation.model.toTransactionUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryFilterUi(
    val id: String,
    val label: String,
    val colorArgb: Long,
)

data class TransactionsState(
    val groups: List<TransactionGroupUi> = emptyList(),
    val categories: List<CategoryFilterUi> = emptyList(),
    val selectedCategoryId: String? = null,
    val query: String = "",
    val isLoading: Boolean = true,
    val hasAnyTransactions: Boolean = false,
    val captureEnabled: Boolean = true,
)

sealed interface TransactionsAction {
    data class OnQueryChange(val query: String) : TransactionsAction
    data class OnCategorySelected(val categoryId: String?) : TransactionsAction
    data object OnAddClick : TransactionsAction
    data object OnOpenCaptureSettings : TransactionsAction
    data object OnRefreshCaptureAccess : TransactionsAction
    data class OnTransactionClick(val id: String) : TransactionsAction
}

sealed interface TransactionsEvent {
    data object OpenAddTransaction : TransactionsEvent
    data object OpenCaptureSettings : TransactionsEvent
    data class OpenTransaction(val id: String) : TransactionsEvent
}

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val notificationAccessChecker: NotificationAccessChecker,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(TransactionsState())
    val state = _state.asStateFlow()

    private val _events = Channel<TransactionsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.observeRecent(RECENT_LIMIT),
                categoryRepository.observeCategories(),
                query,
                selectedCategory,
            ) { transactions, categories, search, categoryId ->
                TransactionsSnapshot(transactions, categories, search, categoryId)
            }.collect { snapshot ->
                _state.update {
                    it.copy(
                        groups = snapshot.filtered().map { tx -> tx.toTransactionUi(snapshot.categoryById[tx.categoryId]) }
                            .groupByDay(),
                        categories = snapshot.categories.map { category ->
                            CategoryFilterUi(category.id, category.name, category.colorArgb)
                        },
                        hasAnyTransactions = snapshot.transactions.isNotEmpty(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onAction(action: TransactionsAction) {
        when (action) {
            is TransactionsAction.OnQueryChange -> {
                query.value = action.query
                _state.update { it.copy(query = action.query) }
            }

            is TransactionsAction.OnCategorySelected -> {
                selectedCategory.value = action.categoryId
                _state.update { it.copy(selectedCategoryId = action.categoryId) }
            }

            TransactionsAction.OnAddClick -> viewModelScope.launch {
                _events.send(TransactionsEvent.OpenAddTransaction)
            }

            TransactionsAction.OnOpenCaptureSettings -> viewModelScope.launch {
                _events.send(TransactionsEvent.OpenCaptureSettings)
            }

            TransactionsAction.OnRefreshCaptureAccess -> _state.update {
                it.copy(captureEnabled = notificationAccessChecker.isEnabled())
            }

            is TransactionsAction.OnTransactionClick -> viewModelScope.launch {
                _events.send(TransactionsEvent.OpenTransaction(action.id))
            }
        }
    }

    private data class TransactionsSnapshot(
        val transactions: List<Transaction>,
        val categories: List<Category>,
        val search: String,
        val categoryId: String?,
    ) {
        val categoryById: Map<String, Category> = categories.associateBy { it.id }

        fun filtered(): List<Transaction> = transactions.filter { transaction ->
            val matchesQuery = search.isBlank() ||
                transaction.merchant.contains(search, ignoreCase = true) ||
                transaction.note?.contains(search, ignoreCase = true) == true
            val matchesCategory = categoryId == null || transaction.categoryId == categoryId
            matchesQuery && matchesCategory
        }
    }

    private companion object {
        const val RECENT_LIMIT = 500
    }
}
