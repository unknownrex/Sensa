package com.unknownrex.sensa.feature.onboarding.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

fun NavGraphBuilder.onboardingGraph(onFinished: () -> Unit) {
    composable<OnboardingRoute> {
        OnboardingRoot(onFinished = onFinished)
    }
}
