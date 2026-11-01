package com.example.myapplication.di.core

import com.example.myapplication.db.DatabaseDriverFactory
import com.example.myapplication.db.DatabaseManager
import kotlinx.datetime.Clock
import org.koin.dsl.module

val coreModule = module {
    single<AppCoroutineDispatchers> { DefaultAppCoroutineDispatchers() }
    single<AppClock> { DefaultAppClock() }
    single<Clock> { Clock.System }
    single<DatabaseDriverFactory> { DatabaseDriverFactory() }
    single<DatabaseManager> { DatabaseManager(get(), get(), get()) }
}
