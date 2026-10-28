package com.example.myapplication.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(vararg platformModules: Module) {
    startKoin {
        modules(koinModules + platformModules)
    }
}

// Helper function for iOS/Swift interop
fun doInitKoin() {
    initKoin()
}
