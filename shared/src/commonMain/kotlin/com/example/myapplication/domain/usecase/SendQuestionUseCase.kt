package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.ai.FinanceAssistant
import com.example.myapplication.domain.model.ChatMessage
import com.example.myapplication.domain.model.MessageRole
import com.example.myapplication.domain.model.MessageStatus
import com.example.myapplication.domain.repository.ChatRepository
import kotlinx.datetime.Clock

class SendQuestionUseCase(
    private val chatRepository: ChatRepository,
    private val financeAssistant: FinanceAssistant
) {
    suspend operator fun invoke(question: String): Result<ChatMessage> {
        // Save user message
        val userMessage = ChatMessage(
            id = generateId(),
            role = MessageRole.User,
            text = question,
            createdAt = Clock.System.now(),
            status = MessageStatus.Sending
        )
        chatRepository.save(userMessage)

        // Call assistant
        val result = financeAssistant.ask(question, emptyList())
        
        return result.fold(
            onSuccess = { assistantMessage ->
                // Update user message to Sent
                val updatedUserMessage = userMessage.copy(status = MessageStatus.Sent)
                chatRepository.save(updatedUserMessage)
                
                // Save assistant message
                chatRepository.save(assistantMessage)
                Result.success(assistantMessage)
            },
            onFailure = { error ->
                // Mark user message as Failed
                val failedUserMessage = userMessage.copy(status = MessageStatus.Failed)
                chatRepository.save(failedUserMessage)
                Result.failure(error)
            }
        )
    }

    suspend fun retry(messageId: String): Result<ChatMessage> {
        // TODO: Implement retry logic - would need to fetch the message and resend
        return Result.failure(Exception("Retry not implemented"))
    }

    private fun generateId(): String {
        return "chat_${Clock.System.now().toEpochMilliseconds()}"
    }
}
