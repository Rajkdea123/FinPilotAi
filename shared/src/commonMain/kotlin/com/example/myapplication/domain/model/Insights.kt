package com.example.myapplication.domain.model

import kotlinx.datetime.Instant

enum class InsightType {
    UnusedSubscription,
    SpendingAlert
}

data class Insight(
    val id: String,
    val type: InsightType,
    val title: String,
    val message: String,
    val createdAt: Instant
)

enum class SubscriptionStatus {
    Active,
    Cancelled
}

data class Subscription(
    val id: String,
    val name: String,
    val amountPaise: Long,
    val renewsInDays: Int,
    val lastUsedDaysAgo: Int?,
    val status: SubscriptionStatus
)
