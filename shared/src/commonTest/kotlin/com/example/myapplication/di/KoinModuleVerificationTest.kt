package com.example.myapplication.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.check.checkModules
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest

class KoinModuleVerificationTest {

    @BeforeTest
    fun setup() {
        stopKoin()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun verifyAllKoinModules() {
        startKoin {
            modules(koinModules)
        }
        
        checkModules {
            // This will verify that all dependencies can be resolved
            // and there are no circular dependencies
        }
    }
}
