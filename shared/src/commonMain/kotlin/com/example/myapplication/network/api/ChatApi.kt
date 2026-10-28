package com.example.myapplication.network.api

import com.example.myapplication.network.dto.ChatMessageDto
import com.example.myapplication.network.dto.CreateChatMessageRequest
import com.example.myapplication.network.dto.ChatResponse

interface ChatApi {
    suspend fun getChatMessages(): List<ChatMessageDto>
    suspend fun createChatMessage(request: CreateChatMessageRequest): ChatResponse
}
