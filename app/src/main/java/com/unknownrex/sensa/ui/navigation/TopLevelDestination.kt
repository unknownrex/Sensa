package com.unknownrex.sensa.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.unknownrex.sensa.feature.dashboard.presentation.DashboardRoute
import com.unknownrex.sensa.feature.settings.presentation.SettingsRoute
import com.unknownrex.sensa.feature.transactions.presentation.TransactionsRoute
import kotlin.reflect.KClass

/** The three primary tabs. Everything else is pushed on top of these. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home(
        route = DashboardRoute,
        routeClass = DashboardRoute::class,
        label = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
    ),
    Transactions(
        route = TransactionsRoute,
        routeClass = TransactionsRoute::class,
        label = "Activity",
        icon = Icons.Outlined.ReceiptLong,
        selectedIcon = Icons.Filled.ReceiptLong,
    ),
    Settings(
        route = SettingsRoute,
        routeClass = SettingsRoute::class,
        label = "Settings",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
    ),
}
