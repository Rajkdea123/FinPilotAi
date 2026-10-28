package com.example.myapplication.ui.navigation

sealed class Route(val path: String) {
    // Authentication flow
    data object Splash : Route("splash")
    data object Onboarding : Route("onboarding")
    data object Login : Route("login")
    data object CreateAccount : Route("create_account")
    data object ForgotPassword : Route("forgot_password")

    // Main tabs (with bottom bar)
    data object Dashboard : Route("dashboard")
    data object Stats : Route("stats")
    data object AI : Route("ai")
    data object Insights : Route("insights")
    data object Sync : Route("sync")

    // Full-screen routes (without bottom bar)
    data object Transactions : Route("transactions")
    data object AddExpense : Route("add_expense")
    data object Profile : Route("profile")

    // Profile sub-routes
    data object EditProfile : Route("edit_profile")
    data object Budget : Route("budget")
    data object Notifications : Route("notifications")
    data object Appearance : Route("appearance")

    // Routes with parameters
    data class AddExpenseEdit(val expenseId: String) : Route("add_expense/$expenseId")
}

// Full-screen routes (no bottom bar)
val fullScreenRoutes = setOf(
    Route.Splash,
    Route.Onboarding,
    Route.Login,
    Route.CreateAccount,
    Route.ForgotPassword,
    Route.Transactions,
    Route.AddExpense,
    Route.AddExpenseEdit(""),
    Route.Profile,
    Route.EditProfile,
    Route.Budget,
    Route.Notifications,
    Route.Appearance
)

// Routes that should clear back stack on navigation
val clearBackStackRoutes = setOf(
    Route.Login,
    Route.CreateAccount,
    Route.Dashboard
)

// Special handling: Sign out should clear back stack and navigate to Login
fun shouldClearBackStackForSignOut(currentRoute: Route, targetRoute: Route): Boolean {
    return currentRoute == Route.Profile && targetRoute == Route.Login
}
