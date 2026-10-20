package com.example.myapplication.domain.model

import kotlinx.datetime.Instant

enum class MessageRole {
    User,
    Assistant
}

enum class MessageStatus {
    Sending,
    Sent,
    Failed
}

data class SpendInsight(
    val label: String,
    val valuePaise: Long,
    val ratio: Float,
    val basedOnCount: Int
)

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val text: String,
    val createdAt: Instant,
    val status: MessageStatus = MessageStatus.Sent,
    val insight: SpendInsight? = null
)
