package com.unknownrex.sensa.feature.review.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import com.unknownrex.sensa.core.domain.usecase.ConfirmTransactionCategory
import com.unknownrex.sensa.core.presentation.UiText
import com.unknownrex.sensa.core.presentation.model.TransactionUi
import com.unknownrex.sensa.core.presentation.model.toTransactionUi
import com.unknownrex.sensa.core.presentation.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewCategoryUi(
    val id: String,
    val label: String,
    val colorArgb: Long,
    val iconKey: String,
)

data class ReviewState(
    val items: List<TransactionUi> = emptyList(),
    val categories: List<ReviewCategoryUi> = emptyList(),
    val isLoading: Boolean = true,
    val confirmingId: String? = null,
)

sealed interface ReviewAction {
    data class OnCategorySelected(val transactionId: String, val categoryId: String) : ReviewAction
    data class OnTransactionClick(val id: String) : ReviewAction
}

sealed interface ReviewEvent {
    data class ShowError(val message: UiText) : ReviewEvent
    data class OpenTransaction(val id: String) : ReviewEvent
}

class ReviewViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val confirmTransactionCategory: ConfirmTransactionCategory,
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewState())
    val state = _state.asStateFlow()

    private val _events = Channel<ReviewEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.observeNeedsReview(),
                categoryRepository.observeCategories(),
            ) { transactions, categories -> transactions to categories }
                .collect { (transactions, categories) ->
                    val byId = categories.associateBy { it.id }
                    _state.update {
                        it.copy(
                            items = transactions.map { transaction -> transaction.toTransactionUi(byId[transaction.categoryId]) },
                            categories = categories
                                .filterNot { category -> category.isUncategorized() }
                                .map { category -> category.toReviewUi() },
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onAction(action: ReviewAction) {
        when (action) {
            is ReviewAction.OnTransactionClick -> viewModelScope.launch {
                _events.send(ReviewEvent.OpenTransaction(action.id))
            }

            is ReviewAction.OnCategorySelected -> confirm(action.transactionId, action.categoryId)
        }
    }

    private fun confirm(transactionId: String, categoryId: String) {
        viewModelScope.launch {
            _state.update { it.copy(confirmingId = transactionId) }
            when (val result = confirmTransactionCategory(transactionId, categoryId)) {
                is Result.Success -> _state.update { it.copy(confirmingId = null) }
                is Result.Error -> {
                    _state.update { it.copy(confirmingId = null) }
                    _events.send(ReviewEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun Category.isUncategorized(): Boolean = id == "uncategorized"

    private fun Category.toReviewUi() = ReviewCategoryUi(
        id = id,
        label = name,
        colorArgb = colorArgb,
        iconKey = iconKey,
    )
}
