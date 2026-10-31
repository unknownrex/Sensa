package com.unknownrex.sensa.feature.transactions.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unknownrex.sensa.core.domain.Result
import com.unknownrex.sensa.core.domain.model.Category
import com.unknownrex.sensa.core.domain.model.PaymentSource
import com.unknownrex.sensa.core.domain.model.Transaction
import com.unknownrex.sensa.core.domain.model.TransactionStatus
import com.unknownrex.sensa.core.domain.newId
import com.unknownrex.sensa.core.domain.repository.CategoryRepository
import com.unknownrex.sensa.core.domain.repository.TransactionRepository
import com.unknownrex.sensa.core.presentation.UiText
import com.unknownrex.sensa.core.presentation.formatRupiahDigits
import com.unknownrex.sensa.core.presentation.parseRupiahInput
import com.unknownrex.sensa.core.presentation.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditorMode { Add, Edit }

data class EditorCategoryUi(
    val id: String,
    val label: String,
    val colorArgb: Long,
    val iconKey: String,
)

data class TransactionEditorState(
    val mode: EditorMode = EditorMode.Add,
    val amountDigits: String = "",
    val merchant: String = "",
    val selectedCategoryId: String? = null,
    val note: String = "",
    val categories: List<EditorCategoryUi> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val amountError: String? = null,
    val merchantError: String? = null,
)

sealed interface TransactionEditorAction {
    data class OnAmountChange(val value: String) : TransactionEditorAction
    data class OnMerchantChange(val value: String) : TransactionEditorAction
    data class OnCategorySelected(val categoryId: String) : TransactionEditorAction
    data class OnNoteChange(val value: String) : TransactionEditorAction
    data object OnSave : TransactionEditorAction
    data object OnDelete : TransactionEditorAction
}

sealed interface TransactionEditorEvent {
    data object Closed : TransactionEditorEvent
    data class ShowError(val message: UiText) : TransactionEditorEvent
}

class TransactionEditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val transactionId: String? = savedStateHandle[KEY_ID]

    private val _state = MutableStateFlow(
        TransactionEditorState(
            mode = if (transactionId == null) EditorMode.Add else EditorMode.Edit,
            amountDigits = savedStateHandle[KEY_AMOUNT] ?: "",
            merchant = savedStateHandle[KEY_MERCHANT] ?: "",
            selectedCategoryId = savedStateHandle[KEY_CATEGORY],
            note = savedStateHandle[KEY_NOTE] ?: "",
        ),
    )
    val state = _state.asStateFlow()

    private val _events = Channel<TransactionEditorEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { categories ->
                _state.update { it.copy(categories = categories.map { category -> category.toEditorUi() }) }
            }
        }
        if (transactionId != null) {
            loadTransaction(transactionId)
        }
    }

    fun onAction(action: TransactionEditorAction) {
        when (action) {
            is TransactionEditorAction.OnAmountChange -> {
                val digits = formatRupiahDigits(action.value)
                savedStateHandle[KEY_AMOUNT] = digits
                _state.update { it.copy(amountDigits = digits, amountError = null) }
            }

            is TransactionEditorAction.OnMerchantChange -> {
                savedStateHandle[KEY_MERCHANT] = action.value
                _state.update { it.copy(merchant = action.value, merchantError = null) }
            }

            is TransactionEditorAction.OnCategorySelected -> {
                savedStateHandle[KEY_CATEGORY] = action.categoryId
                _state.update { it.copy(selectedCategoryId = action.categoryId) }
            }

            is TransactionEditorAction.OnNoteChange -> {
                savedStateHandle[KEY_NOTE] = action.value
                _state.update { it.copy(note = action.value) }
            }

            TransactionEditorAction.OnSave -> save()
            TransactionEditorAction.OnDelete -> delete()
        }
    }

    private fun loadTransaction(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = transactionRepository.getById(id)) {
                is Result.Success -> {
                    val transaction = result.data
                    val digits = formatRupiahDigits(transaction.amountRupiah.toString())
                    savedStateHandle[KEY_AMOUNT] = digits
                    savedStateHandle[KEY_MERCHANT] = transaction.merchant
                    savedStateHandle[KEY_CATEGORY] = transaction.categoryId
                    savedStateHandle[KEY_NOTE] = transaction.note.orEmpty()
                    _state.update {
                        it.copy(
                            amountDigits = digits,
                            merchant = transaction.merchant,
                            selectedCategoryId = transaction.categoryId,
                            note = transaction.note.orEmpty(),
                            isLoading = false,
                        )
                    }
                }

                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(TransactionEditorEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun save() {
        val snapshot = _state.value
        val amount = parseRupiahInput(snapshot.amountDigits)
        val merchant = snapshot.merchant.trim()

        val amountError = if (amount == null) "Enter an amount above zero" else null
        val merchantError = if (merchant.isEmpty()) "Add a merchant or description" else null
        if (amountError != null || merchantError != null) {
            _state.update { it.copy(amountError = amountError, merchantError = merchantError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val existing = transactionId
                ?.let { transactionRepository.getById(it) as? Result.Success }
                ?.data
            val now = System.currentTimeMillis()

            val transaction = Transaction(
                id = existing?.id ?: newId(),
                amountRupiah = amount ?: 0L,
                merchant = merchant,
                categoryId = snapshot.selectedCategoryId ?: existing?.categoryId,
                source = existing?.source ?: PaymentSource.MANUAL,
                timestamp = existing?.timestamp ?: now,
                note = snapshot.note.takeIf { it.isNotBlank() },
                status = TransactionStatus.CONFIRMED,
                confidence = 1f,
                dedupeKey = existing?.dedupeKey,
                createdAt = existing?.createdAt ?: now,
            )

            val result = if (existing == null) {
                transactionRepository.add(transaction)
            } else {
                transactionRepository.update(transaction)
            }

            when (result) {
                is Result.Success -> _events.send(TransactionEditorEvent.Closed)
                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(TransactionEditorEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun delete() {
        val id = transactionId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            when (val result = transactionRepository.delete(id)) {
                is Result.Success -> _events.send(TransactionEditorEvent.Closed)
                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(TransactionEditorEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun Category.toEditorUi() = EditorCategoryUi(
        id = id,
        label = name,
        colorArgb = colorArgb,
        iconKey = iconKey,
    )

    private companion object {
        /** Matches the serialized route argument name of [TransactionEditorRoute]. */
        const val KEY_ID = "transactionId"
        const val KEY_AMOUNT = "amount"
        const val KEY_MERCHANT = "merchant"
        const val KEY_CATEGORY = "category"
        const val KEY_NOTE = "note"
    }
}
