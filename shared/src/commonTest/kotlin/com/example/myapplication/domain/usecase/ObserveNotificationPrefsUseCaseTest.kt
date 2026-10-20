package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeNotificationPrefsRepository
import com.example.myapplication.domain.model.NotificationPrefs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveNotificationPrefsUseCaseTest {

    private lateinit var fakeRepository: FakeNotificationPrefsRepository
    private lateinit var useCase: ObserveNotificationPrefsUseCase

    @BeforeTest
    fun setup() {
        fakeRepository = FakeNotificationPrefsRepository()
        useCase = ObserveNotificationPrefsUseCase(fakeRepository)
    }

    @Test
    fun `initial prefs are default values`() = runTest {
        val prefs = useCase().first()
        assertEquals(NotificationPrefs(), prefs)
    }

    @Test
    fun `observe emits updated prefs after update`() = runTest {
        fakeRepository.update { it.copy(spendingAlerts = false) }
        
        val prefs = useCase().first()
        assertEquals(false, prefs.spendingAlerts)
    }

    @Test
    fun `observe emits multiple updates`() = runTest {
        fakeRepository.update { it.copy(budgetThresholdPercent = 90) }
        assertEquals(90, useCase().first().budgetThresholdPercent)
        
        fakeRepository.update { it.copy(budgetThresholdPercent = 100) }
        assertEquals(100, useCase().first().budgetThresholdPercent)
    }
}
