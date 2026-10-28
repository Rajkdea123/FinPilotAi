package com.example.myapplication.di.usecase

import com.example.myapplication.domain.usecase.CancelSubscriptionUseCase
import com.example.myapplication.domain.usecase.DeleteExpenseUseCase
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
import com.example.myapplication.domain.usecase.ObserveTransactionsUseCase
import com.example.myapplication.domain.usecase.ParseExpenseTextUseCase
import com.example.myapplication.domain.usecase.ResolveConflictUseCase
import com.example.myapplication.domain.usecase.RestoreExpenseUseCase
import com.example.myapplication.domain.usecase.RestoreInsightUseCase
import com.example.myapplication.domain.usecase.SaveExpenseUseCase
import com.example.myapplication.domain.usecase.SendQuestionUseCase
import com.example.myapplication.domain.usecase.SignOutUseCase
import com.example.myapplication.domain.usecase.SyncNowUseCase
import com.example.myapplication.domain.usecase.UpdateNotificationPrefsUseCase
import com.example.myapplication.domain.usecase.UpdateSettingsUseCase
import com.example.myapplication.domain.usecase.ValidateBudgetUseCase
import com.example.myapplication.domain.usecase.ValidateConfirmPasswordUseCase
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
import com.example.myapplication.domain.usecase.ValidateNameUseCase
import com.example.myapplication.domain.usecase.ValidatePasswordUseCase
import com.example.myapplication.domain.usecase.ValidatePhoneUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Auth validation
    factory { ValidateEmailUseCase() }
    factory { ValidatePasswordUseCase() }
    factory { ValidateNameUseCase() }
    factory { ValidateConfirmPasswordUseCase() }
    factory { ValidatePhoneUseCase() }
    factory { ValidateBudgetUseCase() }
    
    // Auth
    factory { SignOutUseCase(get()) }
    
    // Dashboard
    factory { GetMonthSummaryUseCase(get()) }
    factory { GetRecentExpensesUseCase(get()) }
    factory { GetAiInsightUseCase() }
    
    // Add Expense
    factory { ParseExpenseTextUseCase(get()) }
    factory { SaveExpenseUseCase(get()) }
    
    // Transactions
    factory { ObserveTransactionsUseCase(get()) }
    factory { DeleteExpenseUseCase(get()) }
    factory { RestoreExpenseUseCase(get()) }
    
    // Chat
    factory { SendQuestionUseCase(get(), get()) }
    
    // Analytics
    factory { GetAnalyticsUseCase(get()) }
    
    // Insights
    factory { GetInsightsUseCase(get()) }
    factory { DismissInsightUseCase(get()) }
    factory { RestoreInsightUseCase(get()) }
    factory { GetSubscriptionsUseCase(get()) }
    factory { CancelSubscriptionUseCase(get()) }
    
    // Sync
    factory { ObserveSyncStatusUseCase(get()) }
    factory { ResolveConflictUseCase(get()) }
    factory { SyncNowUseCase(get()) }
    factory { UpdateSettingsUseCase(get()) }
    
    // Profile & Edit Profile
    factory { GetProfileUseCase(get()) }
    factory { GetProfileStatsUseCase(get()) }
    
    // Notifications
    factory { ObserveNotificationPrefsUseCase(get()) }
    factory { UpdateNotificationPrefsUseCase(get()) }
    factory { ObservePermissionStatusUseCase(get()) }
}
