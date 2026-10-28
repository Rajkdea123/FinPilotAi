package com.example.myapplication

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.ui.navigation.AppBottomBar
import com.example.myapplication.ui.navigation.AppNavHost
import com.example.myapplication.ui.navigation.Route
import com.example.myapplication.ui.navigation.TabBackStackManager
import com.example.myapplication.ui.navigation.clearBackStackRoutes
import com.example.myapplication.ui.navigation.fullScreenRoutes
import com.example.myapplication.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val settingsRepository: SettingsRepository = koinInject()
    val settings by settingsRepository.observe().collectAsState(AppSettings())

    AppTheme(
        themeMode = settings.themeMode,
        accent = settings.accentColor
    ) {
        var currentRoute by remember { mutableStateOf<Route>(Route.Splash) }
        val tabBackStackManager = rememberTabBackStackManager()

        fun navigate(route: Route, clearBackStack: Boolean = false) {
            // Check if navigating to a bottom tab
            val isBottomTab = route in setOf(
                Route.Dashboard,
                Route.Stats,
                Route.AI,
                Route.Insights,
                Route.Sync
            )

            if (isBottomTab) {
                // Switch to tab - use tab back stack
                tabBackStackManager.setCurrentTab(route)
                currentRoute = route
            } else {
                // Navigate within current tab
                if (clearBackStack) {
                    tabBackStackManager.clearCurrentBackStack()
                } else {
                    tabBackStackManager.push(currentRoute)
                }
                currentRoute = route
            }
        }

        fun navigateBack() {
            val popped = tabBackStackManager.pop()
            if (popped != null) {
                currentRoute = popped
            }
        }

        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            val showBottomBar = currentRoute !in fullScreenRoutes

            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onRouteSelected = { route ->
                        // Bottom nav tabs clear back stack
                        navigate(route, clearBackStack = true)
                    },
                    modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                )
            }

            AppNavHost(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // Check if route should clear back stack
                    val shouldClear = route in clearBackStackRoutes || 
                                     com.example.myapplication.ui.navigation.shouldClearBackStackForSignOut(currentRoute, route)
                    navigate(route, clearBackStack = shouldClear)
                },
                onBack = { navigateBack() }
            )
        }
    }
}