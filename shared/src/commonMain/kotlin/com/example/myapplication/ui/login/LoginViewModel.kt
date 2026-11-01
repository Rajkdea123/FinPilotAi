package com.example.myapplication.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
import com.example.myapplication.domain.usecase.ValidatePasswordUseCase
import com.example.myapplication.platform.storage.UserPreferencesStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val generalError: String? = null,
    val navigateToForgotPassword: Boolean = false,
    val isLoggedIn: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object SignInClicked : LoginEvent()
    object GoogleClicked : LoginEvent()
    object BiometricClicked : LoginEvent()
    object ForgotPasswordClicked : LoginEvent()
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModel(), KoinComponent {
    private val userPreferencesStorage: UserPreferencesStorage by inject()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(
                    email = event.email,
                    emailError = null,
                    generalError = null
                )
            }
            is LoginEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    password = event.password,
                    passwordError = null,
                    generalError = null
                )
            }
            is LoginEvent.TogglePasswordVisibility -> {
                _uiState.value = _uiState.value.copy(
                    isPasswordVisible = !_uiState.value.isPasswordVisible
                )
            }
            is LoginEvent.SignInClicked -> {
                signIn()
            }
            is LoginEvent.GoogleClicked -> {
                // Handled in UI for "Coming soon" snackbar
            }
            is LoginEvent.BiometricClicked -> {
                // Handled in UI for "Coming soon" snackbar
            }
            is LoginEvent.ForgotPasswordClicked -> {
                _uiState.value = _uiState.value.copy(navigateToForgotPassword = true)
            }
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            userPreferencesStorage.setSignedIn(true)
            _uiState.value = _uiState.value.copy(
                isLoggedIn = true,
                emailError = null,
                passwordError = null,
                generalError = null
            )
        }
    }
}
