package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeInsightsRepository
import com.example.myapplication.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CancelSubscriptionUseCaseTest {

    private lateinit var fakeInsightsRepository: FakeInsightsRepository
    private lateinit var cancelSubscriptionUseCase: CancelSubscriptionUseCase

    @BeforeTest
    fun setup() {
        fakeInsightsRepository = FakeInsightsRepository()
        cancelSubscriptionUseCase = CancelSubscriptionUseCase(fakeInsightsRepository)
    }

    @Test
    fun `invoke marks subscription as cancelled`() = runTest {
        val subscriptions = fakeInsightsRepository.observeSubscriptions().first()
        val subscriptionId = subscriptions[0].id

        cancelSubscriptionUseCase(subscriptionId)

        val updatedSubscriptions = fakeInsightsRepository.observeSubscriptions().first()
        val cancelledSubscription = updatedSubscriptions.find { it.id == subscriptionId }
        assertNotNull(cancelledSubscription)
        assertEquals(SubscriptionStatus.Cancelled, cancelledSubscription.status)
    }
}
