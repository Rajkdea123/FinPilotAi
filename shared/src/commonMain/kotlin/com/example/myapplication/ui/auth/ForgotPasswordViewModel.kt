package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    Form,
    Sent
}

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val step: ForgotPasswordStep = ForgotPasswordStep.Form,
    val resendSecondsLeft: Int = 0
)

sealed class ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent()
    object SendResetLinkClicked : ForgotPasswordEvent()
    object ResendClicked : ForgotPasswordEvent()
    object BackToSignIn : ForgotPasswordEvent()
}

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository,
    private val validateEmailUseCase: ValidateEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(
                    email = event.email,
                    emailError = null
                )
            }
            is ForgotPasswordEvent.SendResetLinkClicked -> {
                sendResetLink()
            }
            is ForgotPasswordEvent.ResendClicked -> {
                if (_uiState.value.resendSecondsLeft == 0) {
                    sendResetLink()
                }
            }
            is ForgotPasswordEvent.BackToSignIn -> {
                // Navigation handled by observing this event
            }
        }
    }

    private fun sendResetLink() {
        val emailResult = validateEmailUseCase(_uiState.value.email)

        if (!emailResult.isValid) {
            _uiState.value = _uiState.value.copy(
                emailError = emailResult.errorMessage
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Always show success for privacy, even if email doesn't exist
            when (authRepository.requestPasswordReset(_uiState.value.email)) {
                is com.example.myapplication.domain.model.AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = ForgotPasswordStep.Sent
                    )
                    startCountdown()
                }
                is com.example.myapplication.domain.model.AuthResult.Error -> {
                    // Still show success for privacy
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = ForgotPasswordStep.Sent
                    )
                    startCountdown()
                }
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(resendSecondsLeft = 30)
        
        countdownJob = viewModelScope.launch {
            for (i in 30 downTo 1) {
                delay(1000)
                _uiState.value = _uiState.value.copy(resendSecondsLeft = i)
            }
            _uiState.value = _uiState.value.copy(resendSecondsLeft = 0)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
