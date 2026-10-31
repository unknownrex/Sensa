package com.unknownrex.sensa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unknownrex.sensa.core.designsystem.background.AmbientGlow
import com.unknownrex.sensa.core.designsystem.theme.LocalSensaColors
import com.unknownrex.sensa.core.designsystem.theme.SensaShapes
import com.unknownrex.sensa.core.designsystem.theme.SensaTheme
import com.unknownrex.sensa.feature.dashboard.presentation.DashboardRoute
import com.unknownrex.sensa.feature.dashboard.presentation.dashboardGraph
import com.unknownrex.sensa.feature.onboarding.presentation.OnboardingRoot
import com.unknownrex.sensa.feature.review.presentation.ReviewRoute
import com.unknownrex.sensa.feature.review.presentation.reviewGraph
import com.unknownrex.sensa.feature.settings.presentation.SettingsRoute
import com.unknownrex.sensa.feature.settings.presentation.settingsGraph
import com.unknownrex.sensa.feature.transactions.presentation.TransactionEditorRoute
import com.unknownrex.sensa.feature.transactions.presentation.TransactionsRoute
import com.unknownrex.sensa.feature.transactions.presentation.transactionsGraph
import com.unknownrex.sensa.ui.navigation.TopLevelDestination
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SensaApp() {
    val appViewModel: AppViewModel = koinViewModel()
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val snapshot = settings

    if (snapshot == null) {
        // DataStore has not answered yet. Paint the brand background so there is no white flash.
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF071513)))
        return
    }

    SensaTheme(
        darkTheme = isSystemInDarkTheme(),
        dynamicColor = snapshot.dynamicColor,
        reduceTransparency = snapshot.reduceTransparency,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            AmbientGlow(animated = !snapshot.reduceMotion)
            if (snapshot.onboardingCompleted) {
                MainScaffold()
            } else {
                // Finishing onboarding flips the stored flag, which swaps this branch out.
                OnboardingRoot(onFinished = {})
            }
        }
    }
}

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = LocalSensaColors.current

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val showBottomBar = destination.isTopLevel()
    val showFab = destination?.hasRoute(DashboardRoute::class) == true ||
        destination?.hasRoute(TransactionsRoute::class) == true

    val onShowMessage: (String) -> Unit = remember(snackbarHostState, scope) {
        { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                SensaBottomBar(navController = navController, currentDestination = destination)
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(TransactionEditorRoute()) },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    shape = SensaShapes.sm,
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add transaction")
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            dashboardGraph(
                onAddTransaction = { navController.navigate(TransactionEditorRoute()) },
                onOpenReview = { navController.navigate(ReviewRoute) },
                onOpenTransactions = {
                    navController.navigate(TransactionsRoute) { launchSingleTop = true }
                },
                onOpenTransaction = { id -> navController.navigate(TransactionEditorRoute(id)) },
                onOpenCaptureSettings = {
                    navController.navigate(SettingsRoute) { launchSingleTop = true }
                },
            )
            transactionsGraph(
                navController = navController,
                onOpenSettings = {
                    navController.navigate(SettingsRoute) { launchSingleTop = true }
                },
                onShowMessage = onShowMessage,
            )
            reviewGraph(
                onOpenTransaction = { id -> navController.navigate(TransactionEditorRoute(id)) },
                onShowMessage = onShowMessage,
            )
            settingsGraph()
        }
    }
}

@Composable
private fun SensaBottomBar(
    navController: NavController,
    currentDestination: NavDestination?,
) {
    val colors = LocalSensaColors.current

    NavigationBar(containerColor = colors.surface, tonalElevation = 0.dp) {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy
                ?.any { it.hasRoute(destination.routeClass) } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = {
                    Text(text = destination.label, style = MaterialTheme.typography.labelMedium)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.primary.copy(alpha = 0.14f),
                    unselectedIconColor = colors.onSurfaceMuted,
                    unselectedTextColor = colors.onSurfaceMuted,
                ),
            )
        }
    }
}

private fun NavDestination?.isTopLevel(): Boolean {
    if (this == null) return false
    return TopLevelDestination.entries.any { destination ->
        hierarchy.any { it.hasRoute(destination.routeClass) }
    }
}
