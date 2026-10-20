package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(): Flow<List<ChatMessage>>
    suspend fun save(message: ChatMessage)
}
