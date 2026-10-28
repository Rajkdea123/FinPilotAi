package com.example.myapplication.di

import com.example.myapplication.data.ai.FakeExpenseParser
import com.example.myapplication.data.ai.FakeFinanceAssistant
import com.example.myapplication.data.repository.InMemoryChatRepository
import com.example.myapplication.domain.ai.ExpenseParser
import com.example.myapplication.domain.ai.FinanceAssistant
import com.example.myapplication.domain.repository.ChatRepository
import com.example.myapplication.di.core.coreModule
import com.example.myapplication.di.network.networkModule
import com.example.myapplication.di.platform.platformModule
import com.example.myapplication.di.repository.repositoryModule
import com.example.myapplication.di.sync.syncModule
import com.example.myapplication.di.usecase.useCaseModule
import com.example.myapplication.di.viewmodel.viewModelModule
import com.example.myapplication.platform.biometric.BiometricAuthenticator
import com.example.myapplication.platform.network.getNetworkMonitor
import com.example.myapplication.platform.notification.getNotificationPermissionChecker
import org.koin.dsl.module

val appModule = module {
    // AI components
    single<ExpenseParser> { FakeExpenseParser() }
    single<FinanceAssistant> { FakeFinanceAssistant() }
    
    // Platform components
    factory { BiometricAuthenticator() }
    factory { getNetworkMonitor() }
    factory { getNotificationPermissionChecker() }
}

// Complete Koin module list
val koinModules = listOf(
    coreModule,
    platformModule,
    networkModule,
    repositoryModule,
    syncModule,
    useCaseModule,
    viewModelModule,
    appModule
)
