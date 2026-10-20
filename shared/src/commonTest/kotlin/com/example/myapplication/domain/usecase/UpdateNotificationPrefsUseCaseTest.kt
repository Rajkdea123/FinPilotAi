package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeNotificationPrefsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateNotificationPrefsUseCaseTest {

    private lateinit var fakeRepository: FakeNotificationPrefsRepository
    private lateinit var useCase: UpdateNotificationPrefsUseCase

    @BeforeTest
    fun setup() {
        fakeRepository = FakeNotificationPrefsRepository()
        useCase = UpdateNotificationPrefsUseCase(fakeRepository)
    }

    @Test
    fun `update transforms prefs correctly`() = runTest {
        useCase { it.copy(spendingAlerts = false) }
        
        val prefs = fakeRepository.observe().first()
        assertEquals(false, prefs.spendingAlerts)
    }

    @Test
    fun `update with valid threshold succeeds`() = runTest {
        useCase { it.copy(budgetThresholdPercent = 90) }
        
        val prefs = fakeRepository.observe().first()
        assertEquals(90, prefs.budgetThresholdPercent)
    }

    @Test
    fun `update with invalid threshold throws exception`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase { it.copy(budgetThresholdPercent = 75) }
        }
    }

    @Test
    fun `multiple updates are applied sequentially`() = runTest {
        useCase { it.copy(spendingAlerts = false) }
        useCase { it.copy(weeklySummary = true) }
        useCase { it.copy(budgetThresholdPercent = 90) }
        
        val prefs = fakeRepository.observe().first()
        assertEquals(false, prefs.spendingAlerts)
        assertEquals(true, prefs.weeklySummary)
        assertEquals(90, prefs.budgetThresholdPercent)
    }
}
