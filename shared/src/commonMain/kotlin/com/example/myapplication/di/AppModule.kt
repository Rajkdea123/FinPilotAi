package com.example.myapplication.di

import com.example.myapplication.data.ai.FakeExpenseParser
import com.example.myapplication.data.ai.FakeFinanceAssistant
import com.example.myapplication.data.repository.FakeAuthRepository
import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.data.repository.FakeInsightsRepository
import com.example.myapplication.data.repository.FakeProfileRepository
import com.example.myapplication.data.repository.FakeSettingsRepository
import com.example.myapplication.data.repository.FakeSyncRepository
import com.example.myapplication.data.repository.InMemoryChatRepository
import com.example.myapplication.domain.ai.ExpenseParser
import com.example.myapplication.domain.ai.FinanceAssistant
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.ChatRepository
import com.example.myapplication.domain.repository.ExpenseRepository
import com.example.myapplication.domain.repository.InsightsRepository
import com.example.myapplication.domain.repository.ProfileRepository
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.repository.SyncRepository
import com.example.myapplication.domain.usecase.CancelSubscriptionUseCase
import com.example.myapplication.domain.usecase.DismissInsightUseCase
import com.example.myapplication.domain.usecase.GetAiInsightUseCase
import com.example.myapplication.domain.usecase.GetAnalyticsUseCase
import com.example.myapplication.domain.usecase.GetInsightsUseCase
import com.example.myapplication.domain.usecase.GetMonthSummaryUseCase
import com.example.myapplication.domain.usecase.GetProfileStatsUseCase
import com.example.myapplication.domain.usecase.GetProfileUseCase
import com.example.myapplication.domain.usecase.GetRecentExpensesUseCase
import com.example.myapplication.domain.usecase.GetSubscriptionsUseCase
import com.example.myapplication.domain.usecase.ObserveNotificationPrefsUseCase
import com.example.myapplication.domain.usecase.ObservePermissionStatusUseCase
import com.example.myapplication.domain.usecase.ObserveSyncStatusUseCase
import com.example.myapplication.domain.usecase.ParseExpenseTextUseCase
import com.example.myapplication.domain.usecase.ResolveConflictUseCase
import com.example.myapplication.domain.usecase.RestoreInsightUseCase
import com.example.myapplication.domain.usecase.SaveExpenseUseCase
import com.example.myapplication.domain.usecase.SendQuestionUseCase
import com.example.myapplication.domain.usecase.SignOutUseCase
import com.example.myapplication.domain.usecase.SyncNowUseCase
import com.example.myapplication.domain.usecase.UpdateNotificationPrefsUseCase
import com.example.myapplication.domain.usecase.UpdateSettingsUseCase
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
import com.example.myapplication.domain.usecase.ValidatePasswordUseCase
import com.example.myapplication.platform.biometric.BiometricAuthenticator
import com.example.myapplication.platform.network.getNetworkMonitor
import com.example.myapplication.platform.notification.getNotificationPermissionChecker
import com.example.myapplication.data.repository.FakeBudgetRepository
import com.example.myapplication.data.repository.FakeNotificationPrefsRepository
import com.example.myapplication.domain.repository.BudgetRepository
import com.example.myapplication.domain.repository.NotificationPrefsRepository
import com.example.myapplication.domain.usecase.ValidateBudgetUseCase
import com.example.myapplication.domain.usecase.ValidateNameUseCase
import com.example.myapplication.domain.usecase.ValidatePhoneUseCase
import com.example.myapplication.ui.appearance.AppearanceViewModel
import com.example.myapplication.ui.budget.BudgetViewModel
import com.example.myapplication.ui.notifications.NotificationsViewModel
import com.example.myapplication.ui.profile.edit.EditProfileViewModel
import com.example.myapplication.ui.addexpense.AddExpenseViewModel
import com.example.myapplication.ui.analytics.AnalyticsViewModel
import com.example.myapplication.ui.chat.ChatViewModel
import com.example.myapplication.ui.dashboard.DashboardViewModel
import com.example.myapplication.ui.insights.InsightsViewModel
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.onboarding.OnboardingViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.splash.SplashViewModel
import com.example.myapplication.ui.sync.SyncViewModel
import org.koin.dsl.module

val appModule = module {
    factory { SplashViewModel() }
    factory { OnboardingViewModel() }
    
    // Auth
    single<AuthRepository> { FakeAuthRepository() }
    factory { ValidateEmailUseCase() }
    factory { ValidatePasswordUseCase() }
    factory { SignOutUseCase(get()) }
    factory { LoginViewModel(get(), get(), get()) }
    
    // Dashboard
    single<ExpenseRepository> { FakeExpenseRepository() }
    factory { GetMonthSummaryUseCase(get()) }
    factory { GetRecentExpensesUseCase(get()) }
    factory { GetAiInsightUseCase() }
    factory { DashboardViewModel(get(), get(), get()) }
    
    // Add Expense
    single<ExpenseParser> { FakeExpenseParser() }
    factory { ParseExpenseTextUseCase(get()) }
    factory { SaveExpenseUseCase(get()) }
    factory { AddExpenseViewModel(get(), get()) }
    
    // Chat
    single<ChatRepository> { InMemoryChatRepository() }
    single<FinanceAssistant> { FakeFinanceAssistant() }
    factory { SendQuestionUseCase(get(), get()) }
    factory { ChatViewModel(get(), get()) }

    // Analytics
    factory { GetAnalyticsUseCase(get()) }
    factory { AnalyticsViewModel(get()) }

    // Insights
    single<InsightsRepository> { FakeInsightsRepository() }
    factory { GetInsightsUseCase(get()) }
    factory { DismissInsightUseCase(get()) }
    factory { RestoreInsightUseCase(get()) }
    factory { GetSubscriptionsUseCase(get()) }
    factory { CancelSubscriptionUseCase(get()) }
    factory { InsightsViewModel(get(), get(), get(), get(), get()) }

    // Sync
    single<SyncRepository> { FakeSyncRepository() }
    single<SettingsRepository> { FakeSettingsRepository() }
    factory { getNetworkMonitor() }
    factory { ObserveSyncStatusUseCase(get()) }
    factory { ResolveConflictUseCase(get()) }
    factory { SyncNowUseCase(get()) }
    factory { UpdateSettingsUseCase(get()) }
    factory { SyncViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // Profile & Edit Profile
    single<ProfileRepository> { FakeProfileRepository() }
    factory { GetProfileUseCase(get()) }
    factory { GetProfileStatsUseCase(get()) }
    factory { ValidateNameUseCase() }
    factory { ValidatePhoneUseCase() }
    factory { ProfileViewModel(get(), get(), get(), get()) }
    factory { EditProfileViewModel(get(), get(), get(), get()) }

    // Budget
    single<BudgetRepository> { FakeBudgetRepository() }
    factory { ValidateBudgetUseCase() }
    factory { BudgetViewModel(get(), get(), get()) }

    // Appearance
    factory { AppearanceViewModel(get()) }

    // Notifications
    single<NotificationPrefsRepository> { FakeNotificationPrefsRepository() }
    factory { getNotificationPermissionChecker() }
    factory { ObserveNotificationPrefsUseCase(get()) }
    factory { UpdateNotificationPrefsUseCase(get()) }
    factory { ObservePermissionStatusUseCase(get()) }
    factory { NotificationsViewModel(get(), get(), get(), get(), get(), get()) }
}
