package com.example.myapplication.data.ai

import com.example.myapplication.domain.ai.FinanceAssistant
import com.example.myapplication.domain.model.ChatMessage
import com.example.myapplication.domain.model.MessageRole
import com.example.myapplication.domain.model.MessageStatus
import com.example.myapplication.domain.model.SpendInsight
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

class FakeFinanceAssistant : FinanceAssistant {
    override suspend fun ask(question: String, history: List<ChatMessage>): Result<ChatMessage> {
        // TODO: Replace with Ktor + RAG (embeddings, vector search, LLM) implementation
        delay(900)

        return try {
            val lowerQuestion = question.lowercase()
            val response = when {
                "food" in lowerQuestion -> {
                    ChatMessage(
                        id = generateId(),
                        role = MessageRole.Assistant,
                        text = "You spent ₹8,420 on food last month, 18% more than the month before.",
                        createdAt = Clock.System.now(),
                        status = MessageStatus.Sent,
                        insight = SpendInsight(
                            label = "Food",
                            valuePaise = 842000,
                            ratio = 0.8f,
                            basedOnCount = 46
                        )
                    )
                }
                "too much" in lowerQuestion -> {
                    ChatMessage(
                        id = generateId(),
                        role = MessageRole.Assistant,
                        text = "Food and online shopping rose 23% and 31%. Want a weekly cap for both?",
                        createdAt = Clock.System.now(),
                        status = MessageStatus.Sent,
                        insight = null
                    )
                }
                else -> {
                    ChatMessage(
                        id = generateId(),
                        role = MessageRole.Assistant,
                        text = "I'm here to help you understand your spending. Try asking about specific categories like food, transport, or shopping.",
                        createdAt = Clock.System.now(),
                        status = MessageStatus.Sent,
                        insight = null
                    )
                }
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateId(): String {
        return "chat_${Clock.System.now().toEpochMilliseconds()}"
    }
}
