package com.example.myapplication.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionDto(
    val id: String,
    val name: String,
    @SerialName("amount_paise")
    val amountPaise: Long,
    val category: String,
    @SerialName("billing_cycle")
    val billingCycle: String,
    @SerialName("next_billing_date")
    val nextBillingDate: Long,
    @SerialName("is_active")
    val isActive: Boolean,
    val version: Int,
    @SerialName("updated_at")
    val updatedAt: Long,
    @SerialName("device_id")
    val deviceId: String,
    @SerialName("is_deleted")
    val isDeleted: Boolean,
    @SerialName("sync_state")
    val syncState: String
)

@Serializable
data class CreateSubscriptionRequest(
    val name: String,
    @SerialName("amount_paise")
    val amountPaise: Long,
    val category: String,
    @SerialName("billing_cycle")
    val billingCycle: String,
    @SerialName("next_billing_date")
    val nextBillingDate: Long
)

@Serializable
data class UpdateSubscriptionRequest(
    val id: String,
    val name: String,
    @SerialName("amount_paise")
    val amountPaise: Long,
    val category: String,
    @SerialName("billing_cycle")
    val billingCycle: String,
    @SerialName("next_billing_date")
    val nextBillingDate: Long,
    @SerialName("is_active")
    val isActive: Boolean,
    val version: Int
)
