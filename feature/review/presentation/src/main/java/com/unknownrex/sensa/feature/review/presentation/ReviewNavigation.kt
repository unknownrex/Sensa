package com.unknownrex.sensa.feature.review.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object ReviewRoute

fun NavGraphBuilder.reviewGraph(
    onOpenTransaction: (String) -> Unit,
    onShowMessage: (String) -> Unit,
) {
    composable<ReviewRoute> {
        ReviewRoot(
            onOpenTransaction = onOpenTransaction,
            onShowMessage = onShowMessage,
        )
    }
}
