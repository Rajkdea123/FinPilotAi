package com.example.myapplication.di.sync

import com.example.myapplication.sync.SyncEngine
import com.example.myapplication.sync.SyncManager
import org.koin.dsl.module

val syncModule = module {
    single { SyncEngine(get(), get(), get(), get(), get(), get(), get()) }
    single { SyncManager(get(), get(), get()) }
}
