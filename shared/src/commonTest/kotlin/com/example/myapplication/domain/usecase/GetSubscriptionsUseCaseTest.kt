package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeInsightsRepository
import com.example.myapplication.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetSubscriptionsUseCaseTest {

    private lateinit var fakeInsightsRepository: FakeInsightsRepository
    private lateinit var getSubscriptionsUseCase: GetSubscriptionsUseCase

    @BeforeTest
    fun setup() {
        fakeInsightsRepository = FakeInsightsRepository()
        getSubscriptionsUseCase = GetSubscriptionsUseCase(fakeInsightsRepository)
    }

    @Test
    fun `invoke returns list of subscriptions`() = runTest {
        val subscriptions = getSubscriptionsUseCase().first()

        assertNotNull(subscriptions)
        assertEquals(3, subscriptions.size)
        assertEquals("Swiggy One", subscriptions[0].name)
        assertEquals(SubscriptionStatus.Active, subscriptions[0].status)
    }
}
