package com.unknownrex.sensa.feature.settings.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute

fun NavGraphBuilder.settingsGraph() {
    composable<SettingsRoute> {
        SettingsRoot()
    }
}
