package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.PasswordStrength
import com.example.myapplication.domain.model.calculatePasswordStrength
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.usecase.ValidateConfirmPasswordUseCase
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
import com.example.myapplication.domain.usecase.ValidateNameUseCase
import com.example.myapplication.domain.usecase.ValidatePasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.Weak,
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: String? = null
)

sealed class SignUpEvent {
    data class NameChanged(val name: String) : SignUpEvent()
    data class EmailChanged(val email: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent()
    object TogglePasswordVisibility : SignUpEvent()
    object ToggleConfirmPasswordVisibility : SignUpEvent()
    object TermsToggled : SignUpEvent()
    object SignUpClicked : SignUpEvent()
    object BackToLogin : SignUpEvent()
}

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val validateNameUseCase: ValidateNameUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateConfirmPasswordUseCase: ValidateConfirmPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.NameChanged -> {
                _uiState.value = _uiState.value.copy(
                    name = event.name,
                    nameError = null,
                    generalError = null
                )
            }
            is SignUpEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(
                    email = event.email,
                    emailError = null,
                    generalError = null
                )
            }
            is SignUpEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    password = event.password,
                    passwordError = null,
                    passwordStrength = calculatePasswordStrength(event.password),
                    generalError = null
                )
            }
            is SignUpEvent.ConfirmPasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    confirmPassword = event.confirmPassword,
                    confirmPasswordError = null,
                    generalError = null
                )
            }
            is SignUpEvent.TogglePasswordVisibility -> {
                _uiState.value = _uiState.value.copy(
                    isPasswordVisible = !_uiState.value.isPasswordVisible
                )
            }
            is SignUpEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.value = _uiState.value.copy(
                    isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
                )
            }
            is SignUpEvent.TermsToggled -> {
                _uiState.value = _uiState.value.copy(
                    acceptedTerms = !_uiState.value.acceptedTerms
                )
            }
            is SignUpEvent.SignUpClicked -> {
                signUp()
            }
            is SignUpEvent.BackToLogin -> {
                // Navigation handled by observing this event
            }
        }
    }

    private fun signUp() {
        val nameResult = validateNameUseCase(_uiState.value.name)
        val emailResult = validateEmailUseCase(_uiState.value.email)
        val passwordResult = validatePasswordUseCase(_uiState.value.password)
        val confirmPasswordResult = validateConfirmPasswordUseCase(
            _uiState.value.password,
            _uiState.value.confirmPassword
        )

        if (!nameResult.isValid || !emailResult.isValid || !passwordResult.isValid || !confirmPasswordResult.isValid) {
            _uiState.value = _uiState.value.copy(
                nameError = nameResult.errorMessage,
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage,
                confirmPasswordError = confirmPasswordResult.errorMessage
            )
            return
        }

        if (!_uiState.value.acceptedTerms) {
            _uiState.value = _uiState.value.copy(
                generalError = "You must accept the Terms and Privacy Policy"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = authRepository.signUp(
                _uiState.value.name,
                _uiState.value.email,
                _uiState.value.password
            )) {
                is com.example.myapplication.domain.model.AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Navigation handled by observing success state
                }
                is com.example.myapplication.domain.model.AuthResult.Error -> {
                    if (result.message.contains("Email already in use", ignoreCase = true)) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            emailError = "Email already in use"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                }
            }
        }
    }
}
