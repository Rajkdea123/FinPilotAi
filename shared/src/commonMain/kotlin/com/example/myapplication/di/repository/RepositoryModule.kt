package com.example.myapplication.di.repository

import com.example.myapplication.data.repository.AuthRepositoryImpl
import com.example.myapplication.data.repository.FakeNotificationPrefsRepository
import com.example.myapplication.data.repository.FakeSettingsRepository
import com.example.myapplication.data.repository.FakeSyncRepository
import com.example.myapplication.data.repository.SqlDelightBudgetRepository
import com.example.myapplication.data.repository.SqlDelightChatRepository
import com.example.myapplication.data.repository.SqlDelightExpenseRepository
import com.example.myapplication.data.repository.SqlDelightInsightsRepository
import com.example.myapplication.data.repository.SqlDelightProfileRepository
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.BudgetRepository
import com.example.myapplication.domain.repository.ChatRepository
import com.example.myapplication.domain.repository.ExpenseRepository
import com.example.myapplication.domain.repository.InsightsRepository
import com.example.myapplication.domain.repository.NotificationPrefsRepository
import com.example.myapplication.domain.repository.ProfileRepository
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.repository.SyncRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<ExpenseRepository> { SqlDelightExpenseRepository(get(), get(), get()) }
    single<InsightsRepository> { SqlDelightInsightsRepository(get(), get(), get()) }
    single<ProfileRepository> { SqlDelightProfileRepository(get(), get(), get()) }
    single<SettingsRepository> { FakeSettingsRepository() }
    single<SyncRepository> { FakeSyncRepository() }
    single<ChatRepository> { SqlDelightChatRepository(get(), get(), get()) }
    single<BudgetRepository> { SqlDelightBudgetRepository(get(), get(), get()) }
    single<NotificationPrefsRepository> { FakeNotificationPrefsRepository() }
}
