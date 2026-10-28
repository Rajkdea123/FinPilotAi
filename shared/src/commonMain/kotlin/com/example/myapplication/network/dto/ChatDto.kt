package com.example.myapplication.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    val role: String,
    val content: String,
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
data class CreateChatMessageRequest(
    val role: String,
    val content: String,
    val timestamp: Long
)

@Serializable
data class ChatResponse(
    val message: ChatMessageDto,
    @SerialName("ai_response")
    val aiResponse: String?
)
