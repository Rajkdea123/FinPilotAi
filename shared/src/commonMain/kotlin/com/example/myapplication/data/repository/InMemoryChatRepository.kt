package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.ChatMessage
import com.example.myapplication.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryChatRepository : ChatRepository {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    override fun observeMessages(): Flow<List<ChatMessage>> {
        return _messages.map { it }
    }

    override suspend fun save(message: ChatMessage) {
        // TODO: Replace with SQLDelight implementation
        _messages.value = _messages.value + message
    }
}
