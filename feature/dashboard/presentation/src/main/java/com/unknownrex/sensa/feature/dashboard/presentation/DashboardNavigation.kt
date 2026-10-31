package com.unknownrex.sensa.feature.dashboard.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

fun NavGraphBuilder.dashboardGraph(
    onAddTransaction: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenTransaction: (String) -> Unit,
    onOpenCaptureSettings: () -> Unit,
) {
    composable<DashboardRoute> {
        DashboardRoot(
            onAddTransaction = onAddTransaction,
            onOpenReview = onOpenReview,
            onOpenTransactions = onOpenTransactions,
            onOpenTransaction = onOpenTransaction,
            onOpenCaptureSettings = onOpenCaptureSettings,
        )
    }
}
