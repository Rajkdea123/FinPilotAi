package com.example.myapplication.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.ChatMessage
import com.example.myapplication.domain.repository.ChatRepository
import com.example.myapplication.domain.usecase.SendQuestionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isAssistantTyping: Boolean = false,
    val suggestions: List<String> = listOf(
        "Why did March rise?",
        "Cancel unused subs",
        "How much on food?"
    )
)

sealed class ChatEvent {
    data class InputChanged(val input: String) : ChatEvent()
    object SendClicked : ChatEvent()
    data class SuggestionClicked(val suggestion: String) : ChatEvent()
    data class RetryClicked(val messageId: String) : ChatEvent()
}

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val sendQuestionUseCase: SendQuestionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages().collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InputChanged -> {
                _uiState.value = _uiState.value.copy(input = event.input)
            }
            is ChatEvent.SendClicked -> {
                sendMessage()
            }
            is ChatEvent.SuggestionClicked -> {
                _uiState.value = _uiState.value.copy(input = event.suggestion)
                sendMessage()
            }
            is ChatEvent.RetryClicked -> {
                retryMessage(event.messageId)
            }
        }
    }

    private fun sendMessage() {
        val input = _uiState.value.input.trim()
        if (input.isBlank()) return
        if (_uiState.value.isAssistantTyping) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                input = "",
                isAssistantTyping = true
            )

            sendQuestionUseCase(input)
            
            _uiState.value = _uiState.value.copy(isAssistantTyping = false)
        }
    }

    private fun retryMessage(messageId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAssistantTyping = true)
            
            sendQuestionUseCase.retry(messageId)
            
            _uiState.value = _uiState.value.copy(isAssistantTyping = false)
        }
    }
}
