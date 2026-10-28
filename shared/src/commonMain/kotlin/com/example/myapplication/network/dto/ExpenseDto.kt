package com.example.myapplication.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    val id: String,
    @SerialName("amount_paise")
    val amountPaise: Long,
    val category: String,
    val merchant: String,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("timestamp")
    val timestamp: Long,
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
data class CreateExpenseRequest(
    @SerialName("amount_paise")
    val amountPaise: Long,
    val category: String,
    val merchant: String,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("timestamp")
    val timestamp: Long
)

@Serializable
data class UpdateExpenseRequest(
    val id: String,
    @SerialName("amount_paise")
    val amountPaise: Long,
    val category: String,
    val merchant: String,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("timestamp")
    val timestamp: Long,
    val version: Int
)

@Serializable
data class DeleteExpenseRequest(
    val id: String,
    val version: Int
)
