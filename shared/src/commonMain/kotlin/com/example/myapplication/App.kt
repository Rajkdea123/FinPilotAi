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
import com.example.myapplication.di.appModule
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.navigation.AppBottomBar
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.navigation.AppRoute
import com.example.myapplication.ui.navigation.BottomNavRoute
import com.example.myapplication.ui.onboarding.OnboardingScreen
import com.example.myapplication.ui.splash.SplashScreen
import com.example.myapplication.ui.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val settingsRepository: SettingsRepository = koinInject()
        val settings by settingsRepository.observe().collectAsState(AppSettings())

        AppTheme(
            themeMode = settings.themeMode,
            accent = settings.accentColor
        ) {
            var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.Splash) }
            var currentBottomNavRoute by remember { mutableStateOf<BottomNavRoute>(BottomNavRoute.Home) }

            when (currentRoute) {
                is AppRoute.Splash -> {
                    SplashScreen(
                        onFinished = {
                            currentRoute = AppRoute.Onboarding
                        }
                    )
                }
                is AppRoute.Onboarding -> {
                    OnboardingScreen(
                        onFinished = {
                            currentRoute = AppRoute.Login
                        }
                    )
                }
                is AppRoute.Login -> {
                    LoginScreen(
                        onLoggedIn = {
                            currentRoute = AppRoute.Dashboard
                        },
                        onCreateAccount = {
                            // TODO: Navigate to Sign-up screen
                        }
                    )
                }
                else -> {
                    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                        val isFullScreen = currentRoute in setOf(
                            AppRoute.AddExpense,
                            AppRoute.EditProfile,
                            AppRoute.Budget,
                            AppRoute.Appearance,
                            AppRoute.Notifications,
                            AppRoute.Sync,
                            AppRoute.Insights
                        )

                        if (!isFullScreen) {
                            AppBottomBar(
                                currentRoute = currentBottomNavRoute,
                                onRouteSelected = { bottomRoute ->
                                    when (bottomRoute) {
                                        BottomNavRoute.Home -> currentRoute = AppRoute.Dashboard
                                        BottomNavRoute.Stats -> currentRoute = AppRoute.Stats
                                        BottomNavRoute.Add -> currentRoute = AppRoute.AddExpense
                                        BottomNavRoute.AI -> currentRoute = AppRoute.AI
                                        BottomNavRoute.Profile -> currentRoute = AppRoute.Profile
                                    }
                                    currentBottomNavRoute = bottomRoute
                                },
                                modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                            )
                        }
                        AppNavigation(
                            currentRoute = currentRoute,
                            onRouteChanged = { route ->
                                currentRoute = route
                            },
                            onBottomNavRouteSelected = { bottomRoute ->
                                when (bottomRoute) {
                                    BottomNavRoute.Home -> currentRoute = AppRoute.Dashboard
                                    BottomNavRoute.Stats -> currentRoute = AppRoute.Stats
                                    BottomNavRoute.Add -> currentRoute = AppRoute.AddExpense
                                    BottomNavRoute.AI -> currentRoute = AppRoute.AI
                                    BottomNavRoute.Profile -> currentRoute = AppRoute.Profile
                                }
                                currentBottomNavRoute = bottomRoute
                            },
                            currentBottomNavRoute = currentBottomNavRoute
                        )
                    }
                }
            }
        }
    }
}