package com.unknownrex.sensa.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unknownrex.sensa.core.capture.NotificationAccessChecker
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import com.unknownrex.sensa.core.presentation.formatRupiah
import com.unknownrex.sensa.core.presentation.model.TransactionUi
import com.unknownrex.sensa.core.presentation.model.toTransactionUi
import com.unknownrex.sensa.core.presentation.monthLabel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CategorySliceUi(
    val label: String,
    val amountLabel: String,
    val colorArgb: Long,
    val value: Float,
)

data class DashboardState(
    val monthLabel: String = "",
    val totalLabel: String = formatRupiah(0),
    val transactionCount: Int = 0,
    val slices: List<CategorySliceUi> = emptyList(),
    val recent: List<TransactionUi> = emptyList(),
    val reviewCount: Int = 0,
    val isLoading: Boolean = true,
    val captureEnabled: Boolean = true,
)

sealed interface DashboardAction {
    data object OnAddClick : DashboardAction
    data object OnReviewClick : DashboardAction
    data object OnSeeAllClick : DashboardAction
    data object OnPreviousMonth : DashboardAction
    data object OnNextMonth : DashboardAction
    data object OnEnableCapture : DashboardAction
    data object OnRefreshCaptureAccess : DashboardAction
    data class OnTransactionClick(val id: String) : DashboardAction
}

sealed interface DashboardEvent {
    data object OpenAddTransaction : DashboardEvent
    data object OpenReview : DashboardEvent
    data object OpenTransactions : DashboardEvent
    data object OpenCaptureSettings : DashboardEvent
    data class OpenTransaction(val id: String) : DashboardEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val notificationAccessChecker: NotificationAccessChecker,
) : ViewModel() {

    private val currentMonth = MutableStateFlow(YearMonth.now())
    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                currentMonth.flatMapLatest { month -> transactionRepository.observeBetween(month.startMillis(), month.endMillis()) },
                categoryRepository.observeCategories(),
                transactionRepository.observeNeedsReviewCount(),
            ) { transactions, categories, reviewCount ->
                Triple(transactions, categories, reviewCount)
            }.collect { (transactions, categories, reviewCount) ->
                _state.update { current ->
                    current.copy(
                        monthLabel = monthLabel(currentMonth.value.atDay(1)),
                        totalLabel = formatRupiah(transactions.sumOf { it.amountRupiah }),
                        transactionCount = transactions.size,
                        slices = transactions.toSlices(categories),
                        recent = transactions.take(3).toUi(categories),
                        reviewCount = reviewCount,
                        isLoading = false,
                    )
                }
            }
        }
        _state.update { it.copy(captureEnabled = notificationAccessChecker.isEnabled()) }
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.OnAddClick -> viewModelScope.launch {
                _events.send(DashboardEvent.OpenAddTransaction)
            }

            DashboardAction.OnReviewClick -> viewModelScope.launch {
                _events.send(DashboardEvent.OpenReview)
            }

            DashboardAction.OnSeeAllClick -> viewModelScope.launch {
                _events.send(DashboardEvent.OpenTransactions)
            }

            DashboardAction.OnPreviousMonth -> currentMonth.update { it.minusMonths(1) }
            DashboardAction.OnNextMonth -> currentMonth.update { it.plusMonths(1) }

            DashboardAction.OnEnableCapture -> viewModelScope.launch {
                _events.send(DashboardEvent.OpenCaptureSettings)
            }

            DashboardAction.OnRefreshCaptureAccess -> _state.update {
                it.copy(captureEnabled = notificationAccessChecker.isEnabled())
            }

            is DashboardAction.OnTransactionClick -> viewModelScope.launch {
                _events.send(DashboardEvent.OpenTransaction(action.id))
            }
        }
    }
}

private fun YearMonth.startMillis(): Long =
    atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun YearMonth.endMillis(): Long =
    plusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun List<Transaction>.toUi(categories: List<Category>): List<TransactionUi> {
    val byId = categories.associateBy { it.id }
    return map { it.toTransactionUi(byId[it.categoryId]) }
}

/** Top five categories plus a grouped "Other" slice keeps the donut readable. */
private fun List<Transaction>.toSlices(categories: List<Category>): List<CategorySliceUi> {
    if (isEmpty()) return emptyList()
    val byId = categories.associateBy { it.id }
    val grouped = groupBy { it.categoryId }
        .map { (categoryId, rows) ->
            val category = byId[categoryId]
            CategorySliceUi(
                label = category?.name ?: "Uncategorized",
                amountLabel = formatRupiah(rows.sumOf { it.amountRupiah }),
                colorArgb = category?.colorArgb ?: 0xFF9DB5AE,
                value = rows.sumOf { it.amountRupiah }.toFloat(),
            )
        }
        .sortedByDescending { it.value }

    if (grouped.size <= 5) return grouped

    val top = grouped.take(5)
    val rest = grouped.drop(5)
    return top + CategorySliceUi(
        label = "Other",
        amountLabel = formatRupiah(rest.sumOf { it.value.toLong() }),
        colorArgb = 0xFF6B7F7A,
        value = rest.sumOf { it.value.toDouble() }.toFloat(),
    )
}
