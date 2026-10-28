package com.example.myapplication.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BudgetDto(
    val id: String,
    @SerialName("total_paise")
    val totalPaise: Long,
    val category: String,
    val limit: Long,
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
data class CreateBudgetRequest(
    @SerialName("total_paise")
    val totalPaise: Long,
    val category: String,
    val limit: Long
)

@Serializable
data class UpdateBudgetRequest(
    val id: String,
    @SerialName("total_paise")
    val totalPaise: Long,
    val category: String,
    val limit: Long,
    val version: Int
)
