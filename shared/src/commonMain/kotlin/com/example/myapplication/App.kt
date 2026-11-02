package com.example.myapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.ui.navigation.AppBottomBar
import com.example.myapplication.ui.navigation.AppNavHost
import com.example.myapplication.ui.navigation.Route
import com.example.myapplication.ui.navigation.clearBackStackRoutes
import com.example.myapplication.ui.navigation.fullScreenRoutes
import com.example.myapplication.ui.navigation.rememberTabBackStackManager
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
            val isBottomTab = route in setOf(
                Route.Dashboard,
                Route.Stats,
                Route.AI,
                Route.Insights,
                Route.Sync,
                Route.Profile
            )

            if (isBottomTab) {
                tabBackStackManager.setCurrentTab(route)
                currentRoute = route
            } else {
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
            currentRoute = popped ?: Route.Dashboard
        }

        val showBottomBar = currentRoute !in fullScreenRoutes

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    val currentBottomNavRoute = when (currentRoute) {
                        Route.Dashboard -> com.example.myapplication.ui.navigation.BottomNavRoute.Home
                        Route.Stats -> com.example.myapplication.ui.navigation.BottomNavRoute.Stats
                        Route.AddExpense -> com.example.myapplication.ui.navigation.BottomNavRoute.Add
                        Route.AI -> com.example.myapplication.ui.navigation.BottomNavRoute.AI
                        Route.Profile -> com.example.myapplication.ui.navigation.BottomNavRoute.Profile
                        else -> com.example.myapplication.ui.navigation.BottomNavRoute.Home
                    }

                    AppBottomBar(
                        currentRoute = currentBottomNavRoute,
                        onRouteSelected = { bottomNavRoute ->
                            val (route, shouldClear) = when (bottomNavRoute) {
                                com.example.myapplication.ui.navigation.BottomNavRoute.Home -> Pair(Route.Dashboard, true)
                                com.example.myapplication.ui.navigation.BottomNavRoute.Stats -> Pair(Route.Stats, true)
                                com.example.myapplication.ui.navigation.BottomNavRoute.Add -> Pair(Route.AddExpense, false)
                                com.example.myapplication.ui.navigation.BottomNavRoute.AI -> Pair(Route.AI, true)
                                com.example.myapplication.ui.navigation.BottomNavRoute.Profile -> Pair(Route.Profile, true)
                            }
                            navigate(route, clearBackStack = shouldClear)
                        }
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    val shouldClear = route in clearBackStackRoutes || 
                                     com.example.myapplication.ui.navigation.shouldClearBackStackForSignOut(currentRoute, route)
                    navigate(route, clearBackStack = shouldClear)
                },
                onBack = { navigateBack() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}