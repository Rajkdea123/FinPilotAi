package com.example.myapplication.network.client

import com.example.myapplication.network.api.ChatApi
import com.example.myapplication.network.dto.ChatMessageDto
import com.example.myapplication.network.dto.ChatResponse
import com.example.myapplication.network.dto.CreateChatMessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class ChatApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : ChatApi {

    override suspend fun getChatMessages(): List<ChatMessageDto> {
        return httpClient.get("$baseUrl/chat/messages").body()
    }

    override suspend fun createChatMessage(request: CreateChatMessageRequest): ChatResponse {
        return httpClient.post("$baseUrl/chat/messages") {
            setBody(request)
        }.body()
    }
}
