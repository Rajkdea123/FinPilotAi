package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.addexpense.AddExpenseScreen
import com.example.myapplication.ui.analytics.AnalyticsScreen
import com.example.myapplication.ui.appearance.AppearanceScreen
import com.example.myapplication.ui.auth.CreateAccountScreen
import com.example.myapplication.ui.auth.ForgotPasswordScreen
import com.example.myapplication.ui.budget.BudgetScreen
import com.example.myapplication.ui.chat.ChatScreen
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.insights.InsightsScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.notifications.NotificationsScreen
import com.example.myapplication.ui.onboarding.OnboardingScreen
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.profile.edit.EditProfileScreen
import com.example.myapplication.ui.splash.SplashDestination
import com.example.myapplication.ui.splash.SplashScreen
import com.example.myapplication.ui.splash.SplashViewModel
import com.example.myapplication.ui.sync.SyncScreen
import com.example.myapplication.ui.transactions.TransactionsScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    onBack: () -> Unit
) {
    var selectedExpenseId by remember { mutableStateOf<String?>(null) }
    var profileSnackbarMessage by remember { mutableStateOf<String?>(null) }

    when (currentRoute) {
        // Authentication flow
        Route.Splash -> {
            val splashViewModel: SplashViewModel = koinInject()
            val destination by splashViewModel.destination.collectAsStateWithLifecycle()

            LaunchedEffect(destination) {
                destination?.let { dest ->
                    val route = when (dest) {
                        SplashDestination.Onboarding -> Route.Onboarding
                        SplashDestination.Login -> Route.Login
                        SplashDestination.Dashboard -> Route.Dashboard
                    }
                    onNavigate(route)
                }
            }

            SplashScreen(
                onFinished = {
                    // Navigation handled by SplashViewModel
                }
            )
        }
        Route.Onboarding -> {
            OnboardingScreen(
                onFinished = {
                    onNavigate(Route.Login)
                }
            )
        }
        Route.Login -> {
            LoginScreen(
                onLoggedIn = {
                    onNavigate(Route.Dashboard)
                },
                onCreateAccount = {
                    onNavigate(Route.CreateAccount)
                },
                onForgotPassword = {
                    onNavigate(Route.ForgotPassword)
                }
            )
        }
        Route.CreateAccount -> {
            CreateAccountScreen(
                onSignUpSuccess = {
                    onNavigate(Route.Dashboard)
                },
                onBackToLogin = {
                    onNavigate(Route.Login)
                }
            )
        }
        Route.ForgotPassword -> {
            ForgotPasswordScreen(
                onBackToSignIn = {
                    onNavigate(Route.Login)
                }
            )
        }

        // Main tabs
        Route.Dashboard -> {
            DashboardScreen(
                onNavigateToInsights = { onNavigate(Route.Insights) },
                onNavigateToTransactions = { onNavigate(Route.Transactions) }
            )
        }
        Route.Stats -> {
            AnalyticsScreen()
        }
        Route.AI -> {
            ChatScreen()
        }
        Route.Insights -> {
            InsightsScreen()
        }
        Route.Sync -> {
            SyncScreen()
        }

        // Full-screen routes
        Route.Transactions -> {
            // TODO: Fix TransactionsScreen compilation errors
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                androidx.compose.material3.Text("Transactions Screen - Coming Soon")
            }
        }
        Route.AddExpense -> {
            AddExpenseScreen(
                onBack = { onBack() },
                onExpenseAdded = { onBack() },
                expenseId = null
            )
        }
        is Route.AddExpenseEdit -> {
            AddExpenseScreen(
                onBack = { 
                    selectedExpenseId = null
                    onNavigate(Route.Transactions) 
                },
                onExpenseAdded = { 
                    selectedExpenseId = null
                    onNavigate(Route.Transactions) 
                },
                expenseId = currentRoute.expenseId
            )
        }
        Route.Profile -> {
            ProfileScreen(
                snackbarMessage = profileSnackbarMessage,
                onNavigateToEditProfile = {
                    profileSnackbarMessage = null
                    onNavigate(Route.EditProfile)
                },
                onNavigateToBudget = {
                    profileSnackbarMessage = null
                    onNavigate(Route.Budget)
                },
                onNavigateToDevices = { onNavigate(Route.Sync) },
                onNavigateToNotifications = { onNavigate(Route.Notifications) },
                onNavigateToAppearance = {
                    profileSnackbarMessage = null
                    onNavigate(Route.Appearance)
                },
                onNavigateToLogin = { onNavigate(Route.Login) }
            )
        }

        // Profile sub-routes
        Route.EditProfile -> {
            EditProfileScreen(
                onBack = { message ->
                    profileSnackbarMessage = message
                    onNavigate(Route.Profile)
                }
            )
        }
        Route.Budget -> {
            BudgetScreen(
                onBack = { message ->
                    profileSnackbarMessage = message
                    onNavigate(Route.Profile)
                }
            )
        }
        Route.Notifications -> {
            NotificationsScreen(
                onBack = { onNavigate(Route.Profile) }
            )
        }
        Route.Appearance -> {
            AppearanceScreen(
                onBack = { message ->
                    profileSnackbarMessage = message
                    onNavigate(Route.Profile)
                }
            )
        }
    }
}
