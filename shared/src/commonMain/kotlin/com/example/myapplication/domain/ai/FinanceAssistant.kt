package com.example.myapplication.domain.ai

import com.example.myapplication.domain.model.ChatMessage

interface FinanceAssistant {
    suspend fun ask(question: String, history: List<ChatMessage>): Result<ChatMessage>
}
