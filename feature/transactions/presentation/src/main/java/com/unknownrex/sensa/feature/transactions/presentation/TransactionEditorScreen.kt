package com.unknownrex.sensa.feature.transactions.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unknownrex.sensa.core.designsystem.component.CategoryChip
import com.unknownrex.sensa.core.designsystem.component.SensaAmountField
import com.unknownrex.sensa.core.designsystem.component.SensaDestructiveButton
import com.unknownrex.sensa.core.designsystem.component.SensaPrimaryButton
import com.unknownrex.sensa.core.designsystem.component.SensaTextField
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing
import com.unknownrex.sensa.core.presentation.ObserveAsEvents
import com.unknownrex.sensa.core.presentation.asString
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionEditorRoot(
    onClose: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: TransactionEditorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TransactionEditorEvent.Closed -> onClose()
            is TransactionEditorEvent.ShowError -> onShowMessage(event.message.asString(context))
        }
    }

    TransactionEditorScreen(state = state, onAction = viewModel::onAction, onClose = onClose)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionEditorScreen(
    state: TransactionEditorState,
    onAction: (TransactionEditorAction) -> Unit,
    onClose: () -> Unit,
) {
    val colors = LocalSensaColors.current
    val title = if (state.mode == EditorMode.Add) "Add transaction" else "Edit transaction"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SensaSpacing.screen, vertical = SensaSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onSurface,
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close",
                    tint = colors.onSurfaceMuted,
                )
            }
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else {
            SensaAmountField(
                value = state.amountDigits,
                onValueChange = { onAction(TransactionEditorAction.OnAmountChange(it)) },
                isError = state.amountError != null,
                supportingText = state.amountError,
            )

            SensaTextField(
                value = state.merchant,
                onValueChange = { onAction(TransactionEditorAction.OnMerchantChange(it)) },
                label = "Merchant",
                placeholder = "e.g. Kopi Kenangan",
                isError = state.merchantError != null,
                supportingText = state.merchantError,
                keyboardType = KeyboardType.Text,
            )

            Column(verticalArrangement = Arrangement.spacedBy(SensaSpacing.xs)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceMuted,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
                ) {
                    state.categories.forEach { category ->
                        CategoryChip(
                            label = category.label,
                            color = Color(category.colorArgb),
                            selected = state.selectedCategoryId == category.id,
                            onClick = { onAction(TransactionEditorAction.OnCategorySelected(category.id)) },
                        )
                    }
                }
            }

            SensaTextField(
                value = state.note,
                onValueChange = { onAction(TransactionEditorAction.OnNoteChange(it)) },
                label = "Note (optional)",
                placeholder = "Split with friends, reimbursable…",
                singleLine = false,
                keyboardType = KeyboardType.Text,
            )

            Spacer(modifier = Modifier.height(SensaSpacing.xs))

            SensaPrimaryButton(
                onClick = { onAction(TransactionEditorAction.OnSave) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
            ) {
                Text(
                    text = if (state.isSaving) "Saving…" else "Save",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            if (state.mode == EditorMode.Edit) {
                SensaDestructiveButton(
                    onClick = { onAction(TransactionEditorAction.OnDelete) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                ) {
                    Text(text = "Delete transaction", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
