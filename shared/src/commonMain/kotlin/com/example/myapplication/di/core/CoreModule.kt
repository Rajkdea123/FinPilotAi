package com.example.myapplication.di.core

import org.koin.dsl.module

val coreModule = module {
    single<AppCoroutineDispatchers> { DefaultAppCoroutineDispatchers() }
    single<AppClock> { DefaultAppClock() }
}
