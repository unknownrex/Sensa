package com.unknownrex.sensa.feature.review.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unknownrex.sensa.core.designsystem.component.CategoryChip
import com.unknownrex.sensa.core.designsystem.component.EmptyState
import com.unknownrex.sensa.core.designsystem.component.SensaCard
import com.unknownrex.sensa.core.designsystem.component.ShimmerTransactionRows
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaAmountStyles
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing
import com.unknownrex.sensa.core.presentation.ObserveAsEvents
import com.unknownrex.sensa.core.presentation.asString
import com.unknownrex.sensa.core.presentation.model.TransactionUi
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReviewRoot(
    onOpenTransaction: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: ReviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ReviewEvent.OpenTransaction -> onOpenTransaction(event.id)
            is ReviewEvent.ShowError -> onShowMessage(event.message.asString(context))
        }
    }

    ReviewScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ReviewScreen(
    state: ReviewState,
    onAction: (ReviewAction) -> Unit,
) {
    val colors = LocalSensaColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = SensaSpacing.screen,
            end = SensaSpacing.screen,
            top = SensaSpacing.lg,
            bottom = SensaSpacing.xxl + SensaSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(SensaSpacing.xxs)) {
                Text(
                    text = "Review",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onSurface,
                )
                Text(
                    text = "Pick a category and Sensa remembers this merchant next time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceMuted,
                )
            }
        }

        when {
            state.isLoading -> item { ShimmerTransactionRows(count = 4) }

            state.items.isEmpty() -> item {
                EmptyState(
                    icon = Icons.Outlined.TaskAlt,
                    title = "All caught up",
                    message = "Transactions that need a category will show up here.",
                )
            }

            else -> items(state.items, key = { it.id }) { transaction ->
                ReviewCard(
                    transaction = transaction,
                    categories = state.categories,
                    isConfirming = state.confirmingId == transaction.id,
                    onCategorySelected = { categoryId ->
                        onAction(ReviewAction.OnCategorySelected(transaction.id, categoryId))
                    },
                    onClick = { onAction(ReviewAction.OnTransactionClick(transaction.id)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewCard(
    transaction: TransactionUi,
    categories: List<ReviewCategoryUi>,
    isConfirming: Boolean,
    onCategorySelected: (String) -> Unit,
    onClick: () -> Unit,
) {
    val colors = LocalSensaColors.current
    val amountStyles = LocalSensaAmountStyles.current

    SensaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(
            modifier = Modifier.padding(SensaSpacing.md),
            verticalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchant,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = "${transaction.dayLabel} · ${transaction.timeLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceMuted,
                    )
                }
                Text(
                    text = transaction.amountLabel,
                    style = amountStyles.amount,
                    color = colors.onSurface,
                )
            }

            if (isConfirming) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
                ) {
                    categories.forEach { category ->
                        CategoryChip(
                            label = category.label,
                            color = Color(category.colorArgb),
                            onClick = { onCategorySelected(category.id) },
                        )
                    }
                }
            }
        }
    }
}
