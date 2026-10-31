package com.unknownrex.sensa.feature.transactions.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unknownrex.sensa.core.designsystem.component.CategoryChip
import com.unknownrex.sensa.core.designsystem.component.EmptyState
import com.unknownrex.sensa.core.designsystem.component.SensaTextField
import com.unknownrex.sensa.core.designsystem.component.ShimmerTransactionRows
import com.unknownrex.sensa.core.designsystem.component.TransactionRow
import com.unknownrex.sensa.core.designsystem.icon.CategoryIcons
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing
import com.unknownrex.sensa.core.presentation.ObserveAsEvents
import com.unknownrex.sensa.core.presentation.model.TransactionUi
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionsRoot(
    onAddTransaction: () -> Unit,
    onOpenTransaction: (String) -> Unit,
    onOpenCaptureSettings: () -> Unit,
    viewModel: TransactionsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(TransactionsAction.OnRefreshCaptureAccess)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TransactionsEvent.OpenAddTransaction -> onAddTransaction()
            TransactionsEvent.OpenCaptureSettings -> onOpenCaptureSettings()
            is TransactionsEvent.OpenTransaction -> onOpenTransaction(event.id)
        }
    }

    TransactionsScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    state: TransactionsState,
    onAction: (TransactionsAction) -> Unit,
) {
    val colors = LocalSensaColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SensaSpacing.screen, vertical = SensaSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
        ) {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onSurface,
            )
            if (!state.captureEnabled) {
                NoAccessBanner(onClick = { onAction(TransactionsAction.OnOpenCaptureSettings) })
            }
            SensaTextField(
                value = state.query,
                onValueChange = { onAction(TransactionsAction.OnQueryChange(it)) },
                label = "Search",
                placeholder = "Merchant or note",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
            ) {
                CategoryChip(
                    label = "All",
                    color = colors.primary,
                    selected = state.selectedCategoryId == null,
                    onClick = { onAction(TransactionsAction.OnCategorySelected(null)) },
                )
                state.categories.forEach { category ->
                    CategoryChip(
                        label = category.label,
                        color = Color(category.colorArgb),
                        selected = state.selectedCategoryId == category.id,
                        onClick = { onAction(TransactionsAction.OnCategorySelected(category.id)) },
                    )
                }
            }
        }

        when {
            state.isLoading -> ShimmerTransactionRows(count = 8)

            !state.hasAnyTransactions -> EmptyState(
                icon = Icons.Outlined.ReceiptLong,
                title = "No transactions yet",
                message = "Add one manually, or let Sensa capture GoPay and BCA mobile " +
                    "payment notifications.",
                modifier = Modifier.padding(top = SensaSpacing.xl),
            )

            state.groups.isEmpty() -> EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "Nothing matches",
                message = "Try a different search or clear the category filter.",
                modifier = Modifier.padding(top = SensaSpacing.xl),
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = SensaSpacing.screen,
                    end = SensaSpacing.screen,
                    bottom = SensaSpacing.xxl + SensaSpacing.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(SensaSpacing.xxs),
            ) {
                state.groups.forEach { group ->
                    stickyHeader(key = "header-${group.label}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = SensaSpacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = group.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceMuted,
                            )
                            Text(
                                text = group.totalLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceMuted,
                            )
                        }
                    }
                    items(group.transactions, key = { it.id }) { transaction ->
                        TransactionsRow(
                            transaction = transaction,
                            onClick = { onAction(TransactionsAction.OnTransactionClick(transaction.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoAccessBanner(onClick: () -> Unit) {
    val colors = LocalSensaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SensaShapes.sm)
            .background(colors.warning.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = SensaSpacing.md, vertical = SensaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsActive,
            contentDescription = null,
            tint = colors.warning,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "Turn on notification access so payments are captured automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun TransactionsRow(transaction: TransactionUi, onClick: () -> Unit) {    TransactionRow(
        icon = CategoryIcons.iconFor(transaction.iconKey),
        iconTint = Color(transaction.categoryColorArgb),
        title = transaction.merchant,
        subtitle = "${transaction.categoryLabel} · ${transaction.timeLabel}",
        amount = transaction.amountLabel,
        onClick = onClick,
        trailingLabel = if (transaction.needsReview) "Review" else null,
    )
}
