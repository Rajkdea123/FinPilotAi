package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.Insight
import com.example.myapplication.domain.model.InsightType
import com.example.myapplication.domain.model.Subscription
import com.example.myapplication.domain.model.SubscriptionStatus
import com.example.myapplication.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class FakeInsightsRepository : InsightsRepository {

    private val _insights = MutableStateFlow(
        listOf(
            Insight(
                id = "1",
                type = InsightType.UnusedSubscription,
                title = "Unused subscription",
                message = "You have not used your Swiggy subscription in 25 days. Consider cancelling it.",
                createdAt = Clock.System.now()
            ),
            Insight(
                id = "2",
                type = InsightType.SpendingAlert,
                title = "Spending alert",
                message = "Your food spending is 28% higher than your monthly average.",
                createdAt = Clock.System.now()
            )
        )
    )

    private val _subscriptions = MutableStateFlow(
        listOf(
            Subscription(
                id = "1",
                name = "Swiggy One",
                amountPaise = 149_00L,
                renewsInDays = 6,
                lastUsedDaysAgo = 25,
                status = SubscriptionStatus.Active
            ),
            Subscription(
                id = "2",
                name = "Streaming",
                amountPaise = 199_00L,
                renewsInDays = 12,
                lastUsedDaysAgo = null,
                status = SubscriptionStatus.Active
            ),
            Subscription(
                id = "3",
                name = "Cloud storage",
                amountPaise = 130_00L,
                renewsInDays = 20,
                lastUsedDaysAgo = null,
                status = SubscriptionStatus.Active
            )
        )
    )

    // TODO: Replace with Ktor + AI-generated insights and SQLDelight implementation

    override fun observeInsights(): Flow<List<Insight>> {
        return _insights
    }

    override suspend fun dismiss(id: String) {
        _insights.value = _insights.value.filterNot { it.id == id }
    }

    override suspend fun restore(id: String) {
        // In a real implementation, this would restore from a dismissed list
        // For now, we'll just add a sample insight back
        val sampleInsight = Insight(
            id = id,
            type = InsightType.UnusedSubscription,
            title = "Unused subscription",
            message = "You have not used your subscription recently.",
            createdAt = Clock.System.now()
        )
        _insights.value = _insights.value + sampleInsight
    }

    override fun observeSubscriptions(): Flow<List<Subscription>> {
        return _subscriptions
    }

    override suspend fun markCancelled(id: String) {
        _subscriptions.value = _subscriptions.value.map { sub ->
            if (sub.id == id) {
                sub.copy(status = SubscriptionStatus.Cancelled)
            } else {
                sub
            }
        }
    }
}
