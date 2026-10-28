package com.example.myapplication.di.viewmodel

import com.example.myapplication.ui.addexpense.AddExpenseViewModel
import com.example.myapplication.ui.analytics.AnalyticsViewModel
import com.example.myapplication.ui.appearance.AppearanceViewModel
import com.example.myapplication.ui.budget.BudgetViewModel
import com.example.myapplication.ui.chat.ChatViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.insights.InsightsViewModel
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.notifications.NotificationsViewModel
import com.example.myapplication.ui.onboarding.OnboardingViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.profile.edit.EditProfileViewModel
import com.example.myapplication.ui.splash.SplashViewModel
import com.example.myapplication.ui.sync.SyncViewModel
import com.example.myapplication.ui.transactions.TransactionsViewModel
import com.example.myapplication.ui.auth.ForgotPasswordViewModel
import com.example.myapplication.ui.auth.SignUpViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { SplashViewModel() }
    factory { OnboardingViewModel() }
    
    // Auth
    factory { LoginViewModel(get(), get(), get()) }
    factory { SignUpViewModel(get(), get(), get(), get(), get()) }
    factory { ForgotPasswordViewModel(get(), get()) }
    
    // Dashboard
    factory { DashboardViewModel(get(), get(), get()) }
    
    // Add Expense
    factory { AddExpenseViewModel(get(), get(), get()) }
    
    // Chat
    factory { ChatViewModel(get(), get()) }

    // Analytics
    factory { AnalyticsViewModel(get()) }

    // Insights
    factory { InsightsViewModel(get(), get(), get(), get(), get()) }

    // Sync
    factory { SyncViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // Profile & Edit Profile
    factory { ProfileViewModel(get(), get(), get(), get()) }
    factory { EditProfileViewModel(get(), get(), get(), get()) }

    // Budget
    factory { BudgetViewModel(get(), get(), get()) }

    // Appearance
    factory { AppearanceViewModel(get()) }

    // Notifications
    factory { NotificationsViewModel(get(), get(), get(), get(), get(), get()) }
    
    // Transactions
    factory { TransactionsViewModel(get(), get(), get()) }
}
