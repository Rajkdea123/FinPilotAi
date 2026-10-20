package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeInsightsRepository
import com.example.myapplication.domain.model.InsightType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetInsightsUseCaseTest {

    private lateinit var fakeInsightsRepository: FakeInsightsRepository
    private lateinit var getInsightsUseCase: GetInsightsUseCase

    @BeforeTest
    fun setup() {
        fakeInsightsRepository = FakeInsightsRepository()
        getInsightsUseCase = GetInsightsUseCase(fakeInsightsRepository)
    }

    @Test
    fun `invoke returns list of insights`() = runTest {
        val insights = getInsightsUseCase().first()

        assertNotNull(insights)
        assertEquals(2, insights.size)
        assertEquals(InsightType.UnusedSubscription, insights[0].type)
        assertEquals(InsightType.SpendingAlert, insights[1].type)
    }
}
