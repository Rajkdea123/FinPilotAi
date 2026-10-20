package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.addexpense.AddExpenseScreen
import com.example.myapplication.ui.analytics.AnalyticsScreen
import com.example.myapplication.ui.appearance.AppearanceScreen
import com.example.myapplication.ui.budget.BudgetScreen
import com.example.myapplication.ui.chat.ChatScreen
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.insights.InsightsScreen
import com.example.myapplication.ui.notifications.NotificationsScreen
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.profile.edit.EditProfileScreen
import com.example.myapplication.ui.sync.SyncScreen
import com.example.myapplication.ui.theme.AppTheme

sealed class AppRoute {
    object Splash : AppRoute()
    object Onboarding : AppRoute()
    object Login : AppRoute()
    object Dashboard : AppRoute()
    object Stats : AppRoute()
    object Insights : AppRoute()
    object Sync : AppRoute()
    object AddExpense : AppRoute()
    object AI : AppRoute()
    object Profile : AppRoute()
    object EditProfile : AppRoute()
    object Budget : AppRoute()
    object Notifications : AppRoute()
    object Appearance : AppRoute()
}

@Composable
fun AppNavigation(
    currentRoute: AppRoute,
    onRouteChanged: (AppRoute) -> Unit,
    onBottomNavRouteSelected: (BottomNavRoute) -> Unit,
    currentBottomNavRoute: BottomNavRoute
) {
    var profileSnackbarMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentRoute) {
            is AppRoute.Splash -> {
                // SplashScreen will be handled in App.kt
            }
            is AppRoute.Onboarding -> {
                // OnboardingScreen will be handled in App.kt
            }
            is AppRoute.Login -> {
                // LoginScreen will be handled in App.kt
            }
            is AppRoute.Dashboard -> {
                DashboardScreen(
                    onNavigateToInsights = { onRouteChanged(AppRoute.Insights) }
                )
            }
            is AppRoute.Stats -> {
                AnalyticsScreen()
            }
            is AppRoute.Insights -> {
                InsightsScreen()
            }
            is AppRoute.Sync -> {
                SyncScreen()
            }
            is AppRoute.AddExpense -> {
                AddExpenseScreen(
                    onBack = { onRouteChanged(AppRoute.Dashboard) },
                    onExpenseAdded = { onRouteChanged(AppRoute.Dashboard) }
                )
            }
            is AppRoute.AI -> {
                ChatScreen()
            }
            is AppRoute.Profile -> {
                ProfileScreen(
                    snackbarMessage = profileSnackbarMessage,
                    onNavigateToEditProfile = {
                        profileSnackbarMessage = null
                        onRouteChanged(AppRoute.EditProfile)
                    },
                    onNavigateToBudget = {
                        profileSnackbarMessage = null
                        onRouteChanged(AppRoute.Budget)
                    },
                    onNavigateToDevices = { onRouteChanged(AppRoute.Sync) },
                    onNavigateToNotifications = { onRouteChanged(AppRoute.Notifications) },
                    onNavigateToAppearance = {
                        profileSnackbarMessage = null
                        onRouteChanged(AppRoute.Appearance)
                    },
                    onNavigateToLogin = { onRouteChanged(AppRoute.Login) }
                )
            }
            is AppRoute.EditProfile -> {
                EditProfileScreen(
                    onBack = { message ->
                        profileSnackbarMessage = message
                        onRouteChanged(AppRoute.Profile)
                    }
                )
            }
            is AppRoute.Budget -> {
                BudgetScreen(
                    onBack = { message ->
                        profileSnackbarMessage = message
                        onRouteChanged(AppRoute.Profile)
                    }
                )
            }
            is AppRoute.Notifications -> {
                NotificationsScreen(
                    onBack = { onRouteChanged(AppRoute.Profile) }
                )
            }
            is AppRoute.Appearance -> {
                AppearanceScreen(
                    onBack = { message ->
                        profileSnackbarMessage = message
                        onRouteChanged(AppRoute.Profile)
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, onBack: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            androidx.compose.material3.IconButton(
                onClick = onBack,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(AppIcons.BackIcon),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
