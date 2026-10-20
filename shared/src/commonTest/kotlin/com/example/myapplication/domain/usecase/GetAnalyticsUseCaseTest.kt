package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.domain.model.Period
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetAnalyticsUseCaseTest {

    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var getAnalyticsUseCase: GetAnalyticsUseCase

    @BeforeTest
    fun setup() {
        fakeExpenseRepository = FakeExpenseRepository()
        getAnalyticsUseCase = GetAnalyticsUseCase(fakeExpenseRepository)
    }

    @Test
    fun `invoke week returns week analytics summary`() = runTest {
        val summary = getAnalyticsUseCase(Period.Week).first()

        assertNotNull(summary)
        assertEquals(Period.Week, summary.period)
        assertEquals(9840_00L, summary.totalPaise)
        assertEquals(7, summary.points.size)
        assertEquals(4, summary.highlightIndex)
    }

    @Test
    fun `invoke month returns month analytics summary`() = runTest {
        val summary = getAnalyticsUseCase(Period.Month).first()

        assertNotNull(summary)
        assertEquals(Period.Month, summary.period)
        assertEquals(4, summary.points.size)
        assertEquals(1, summary.highlightIndex)
    }

    @Test
    fun `invoke year returns year analytics summary`() = runTest {
        val summary = getAnalyticsUseCase(Period.Year).first()

        assertNotNull(summary)
        assertEquals(Period.Year, summary.period)
        assertEquals(12, summary.points.size)
        assertEquals(9, summary.highlightIndex)
    }
}
