package com.example.myapplication.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("avatar_url")
    val avatarUrl: String?,
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
data class UpdateProfileRequest(
    val name: String,
    val email: String,
    @SerialName("avatar_url")
    val avatarUrl: String?
)
