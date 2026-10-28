package com.example.myapplication.di.platform

import com.example.myapplication.db.DatabaseDriverFactory
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.di.AppCoroutineDispatchers
import com.example.myapplication.platform.network.getNetworkMonitor
import com.example.myapplication.platform.storage.InMemoryUserPreferencesStorage
import com.example.myapplication.platform.storage.SecureTokenStorage
import com.example.myapplication.platform.storage.TokenStorage
import com.example.myapplication.platform.storage.UserPreferencesStorage
import org.koin.dsl.module

// Platform-specific module - to be extended in Android and iOS source sets
val platformModule = module {
    single { DatabaseDriverFactory() }
    single { DatabaseManager(get(), get(), get()) }
    single<UserPreferencesStorage> { InMemoryUserPreferencesStorage() }
    single<TokenStorage> { SecureTokenStorage() }
    factory { getNetworkMonitor() }
    // Platform-specific implementations will be added here
    // e.g., secure storage, database driver factory, network monitor, etc.
}
