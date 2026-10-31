package com.unknownrex.sensa.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unknownrex.sensa.core.designsystem.chart.DonutChart
import com.unknownrex.sensa.core.designsystem.chart.DonutSegment
import com.unknownrex.sensa.core.designsystem.component.EmptyState
import com.unknownrex.sensa.core.designsystem.component.SectionHeader
import com.unknownrex.sensa.core.designsystem.component.ShimmerTransactionRows
import com.unknownrex.sensa.core.designsystem.component.TransactionRow
import com.unknownrex.sensa.core.designsystem.glass.GlassBox
import com.unknownrex.sensa.core.designsystem.icon.CategoryIcons
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaSpacing
import com.unknownrex.sensa.core.designsystem.theme.SensaTheme
import com.unknownrex.sensa.core.presentation.ObserveAsEvents
import com.unknownrex.sensa.core.presentation.model.TransactionUi
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardRoot(
    onAddTransaction: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenTransaction: (String) -> Unit,
    onOpenCaptureSettings: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(DashboardAction.OnRefreshCaptureAccess)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            DashboardEvent.OpenAddTransaction -> onAddTransaction()
            DashboardEvent.OpenReview -> onOpenReview()
            DashboardEvent.OpenTransactions -> onOpenTransactions()
            DashboardEvent.OpenCaptureSettings -> onOpenCaptureSettings()
            is DashboardEvent.OpenTransaction -> onOpenTransaction(event.id)
        }
    }

    DashboardScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(SensaSpacing.md),
    ) {
        item {
            MonthHeader(
                monthLabel = state.monthLabel,
                onPrevious = { onAction(DashboardAction.OnPreviousMonth) },
                onNext = { onAction(DashboardAction.OnNextMonth) },
            )
        }

        item {
            Column {
                Text(
                    text = "Spent this month",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceMuted,
                )
                Text(
                    text = state.totalLabel,
                    style = MaterialTheme.typography.displayLarge,
                    color = colors.onSurface,
                )
                Text(
                    text = "${state.transactionCount} transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceMuted,
                )
            }
        }

        if (state.reviewCount > 0) {
            item {
                ReviewBanner(count = state.reviewCount, onClick = { onAction(DashboardAction.OnReviewClick) })
            }
        }

        if (!state.captureEnabled) {
            item {
                CaptureBanner(onClick = { onAction(DashboardAction.OnEnableCapture) })
            }
        }

        item {
            GlassBox(modifier = Modifier.fillMaxWidth(), blurRadius = 24.dp) {
                Column(modifier = Modifier.padding(SensaSpacing.lg)) {
                    Text(
                        text = "By category",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(SensaSpacing.md))
                    if (state.slices.isEmpty()) {
                        Text(
                            text = "No spending recorded this month yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceMuted,
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SensaSpacing.lg),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DonutChart(
                                segments = state.slices.map {
                                    DonutSegment(
                                        label = it.label,
                                        value = it.value,
                                        color = Color(it.colorArgb),
                                    )
                                },
                                modifier = Modifier.size(132.dp),
                                contentDescription = state.slices.joinToString(", ") {
                                    "${it.label} ${it.amountLabel}"
                                },
                                center = {
                                    Text(
                                        text = "${state.slices.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = colors.onSurface,
                                    )
                                },
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
                            ) {
                                state.slices.take(4).forEach { slice ->
                                    LegendRow(label = slice.label, amount = slice.amountLabel, color = Color(slice.colorArgb))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Recent",
                action = {
                    TextButton(onClick = { onAction(DashboardAction.OnSeeAllClick) }) {
                        Text(text = "See all", style = MaterialTheme.typography.labelLarge)
                    }
                },
            )
        }

        when {
            state.isLoading -> item { ShimmerTransactionRows(count = 3) }

            state.recent.isEmpty() -> item {
                EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No spending yet",
                    message = "Transactions appear here automatically when GoPay or BCA mobile " +
                        "send a payment notification.",
                )
            }

            else -> items(state.recent, key = { it.id }) { transaction ->
                RecentRow(transaction = transaction, onClick = { onAction(DashboardAction.OnTransactionClick(transaction.id)) })
            }
        }
    }
}

@Composable
private fun MonthHeader(
    monthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = LocalSensaColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Sensa",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = "Previous month",
                tint = colors.onSurfaceMuted,
                modifier = Modifier
                    .size(SensaSpacing.minTouchTarget)
                    .clip(CircleShape)
                    .clickable(onClick = onPrevious)
                    .padding(SensaSpacing.sm),
            )
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = SensaSpacing.xs),
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Next month",
                tint = colors.onSurfaceMuted,
                modifier = Modifier
                    .size(SensaSpacing.minTouchTarget)
                    .clip(CircleShape)
                    .clickable(onClick = onNext)
                    .padding(SensaSpacing.sm),
            )
        }
    }
}

@Composable
private fun ReviewBanner(count: Int, onClick: () -> Unit) {
    val colors = LocalSensaColors.current
    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = com.unknownrex.sensa.core.designsystem.theme.SensaShapes.md,
        blurRadius = 18.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SensaSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.warning.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$count to review",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                )
                Text(
                    text = "Give these transactions a category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceMuted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceMuted,
            )
        }
    }
}

@Composable
private fun CaptureBanner(onClick: () -> Unit) {
    val colors = LocalSensaColors.current
    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = com.unknownrex.sensa.core.designsystem.theme.SensaShapes.md,
        blurRadius = 18.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SensaSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SensaSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Turn on capture",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                )
                Text(
                    text = "Allow notification access so payments are logged automatically",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceMuted,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceMuted,
            )
        }
    }
}

@Composable
private fun LegendRow(label: String, amount: String, color: Color) {
    val colors = LocalSensaColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SensaSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun RecentRow(transaction: TransactionUi, onClick: () -> Unit) {
    TransactionRow(
        icon = CategoryIcons.iconFor(transaction.iconKey),
        iconTint = Color(transaction.categoryColorArgb),
        title = transaction.merchant,
        subtitle = "${transaction.categoryLabel} · ${transaction.timeLabel}",
        amount = transaction.amountLabel,
        onClick = onClick,
        trailingLabel = if (transaction.needsReview) "Review" else null,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF071513)
@Composable
private fun DashboardScreenPreview() {
    SensaTheme(darkTheme = true) {
        DashboardScreen(
            state = DashboardState(
                monthLabel = "Oct 2026",
                totalLabel = "Rp1.240.000",
                transactionCount = 12,
                reviewCount = 2,
                slices = listOf(
                    CategorySliceUi("Food and drink", "Rp420.000", 0xFFFFB454, 420f),
                    CategorySliceUi("Transport", "Rp310.000", 0xFF6AB7FF, 310f),
                    CategorySliceUi("Groceries", "Rp510.000", 0xFF8BE28B, 510f),
                ),
            ),
            onAction = {},
        )
    }
}
