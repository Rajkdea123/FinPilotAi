package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeInsightsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DismissInsightUseCaseTest {

    private lateinit var fakeInsightsRepository: FakeInsightsRepository
    private lateinit var dismissInsightUseCase: DismissInsightUseCase

    @BeforeTest
    fun setup() {
        fakeInsightsRepository = FakeInsightsRepository()
        dismissInsightUseCase = DismissInsightUseCase(fakeInsightsRepository)
    }

    @Test
    fun `invoke removes insight from list`() = runTest {
        val initialInsights = fakeInsightsRepository.observeInsights().first()
        assertEquals(2, initialInsights.size)

        dismissInsightUseCase(initialInsights[0].id)

        val updatedInsights = fakeInsightsRepository.observeInsights().first()
        assertEquals(1, updatedInsights.size)
        assertEquals(initialInsights[1].id, updatedInsights[0].id)
    }
}
