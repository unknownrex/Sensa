package com.unknownrex.sensa.feature.transactions.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object TransactionsRoute

@Serializable
data class TransactionEditorRoute(val transactionId: String? = null)

fun NavGraphBuilder.transactionsGraph(
    navController: NavController,
    onOpenSettings: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    composable<TransactionsRoute> {
        TransactionsRoot(
            onAddTransaction = { navController.navigate(TransactionEditorRoute()) },
            onOpenTransaction = { id -> navController.navigate(TransactionEditorRoute(id)) },
            onOpenCaptureSettings = onOpenSettings,
        )
    }
    composable<TransactionEditorRoute> {
        // SavedStateHandle is filled from the serialized route arguments, so no id is threaded
        // through the composable.
        TransactionEditorRoot(
            onClose = { navController.popBackStack() },
            onShowMessage = onShowMessage,
        )
    }
}
