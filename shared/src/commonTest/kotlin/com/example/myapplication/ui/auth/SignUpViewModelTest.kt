package com.example.myapplication.ui.auth

import com.example.myapplication.data.repository.FakeAuthRepository
import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.usecase.ValidateConfirmPasswordUseCase
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
import com.example.myapplication.domain.usecase.ValidateNameUseCase
import com.example.myapplication.domain.usecase.ValidatePasswordUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var validateNameUseCase: ValidateNameUseCase
    private lateinit var validateEmailUseCase: ValidateEmailUseCase
    private lateinit var validatePasswordUseCase: ValidatePasswordUseCase
    private lateinit var validateConfirmPasswordUseCase: ValidateConfirmPasswordUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        validateNameUseCase = ValidateNameUseCase()
        validateEmailUseCase = ValidateEmailUseCase()
        validatePasswordUseCase = ValidatePasswordUseCase()
        validateConfirmPasswordUseCase = ValidateConfirmPasswordUseCase()
        viewModel = SignUpViewModel(
            authRepository = fakeAuthRepository,
            validateNameUseCase = validateNameUseCase,
            validateEmailUseCase = validateEmailUseCase,
            validatePasswordUseCase = validatePasswordUseCase,
            validateConfirmPasswordUseCase = validateConfirmPasswordUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `name change updates state and clears error`() = runTest {
        viewModel.onEvent(SignUpEvent.NameChanged("John Doe"))
        assertEquals("John Doe", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `email change updates state and clears error`() = runTest {
        viewModel.onEvent(SignUpEvent.EmailChanged("test@example.com"))
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `password change updates state and clears error`() = runTest {
        viewModel.onEvent(SignUpEvent.PasswordChanged("password123"))
        assertEquals("password123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `confirm password change updates state and clears error`() = runTest {
        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password123"))
        assertEquals("password123", viewModel.uiState.value.confirmPassword)
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `toggle password visibility updates state`() = runTest {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onEvent(SignUpEvent.TogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.onEvent(SignUpEvent.TogglePasswordVisibility)
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun `toggle confirm password visibility updates state`() = runTest {
        assertFalse(viewModel.uiState.value.isConfirmPasswordVisible)
        viewModel.onEvent(SignUpEvent.ToggleConfirmPasswordVisibility)
        assertTrue(viewModel.uiState.value.isConfirmPasswordVisible)
    }

    @Test
    fun `terms toggle updates state`() = runTest {
        assertFalse(viewModel.uiState.value.acceptedTerms)
        viewModel.onEvent(SignUpEvent.TermsToggled)
        assertTrue(viewModel.uiState.value.acceptedTerms)
        viewModel.onEvent(SignUpEvent.TermsToggled)
        assertFalse(viewModel.uiState.value.acceptedTerms)
    }

    @Test
    fun `sign up with invalid name shows error`() = runTest {
        viewModel.onEvent(SignUpEvent.NameChanged("J"))
        viewModel.onEvent(SignUpEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(SignUpEvent.PasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.TermsToggled)
        viewModel.onEvent(SignUpEvent.SignUpClicked)
        assertEquals("Name must be at least 2 characters", viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign up with invalid email shows error`() = runTest {
        viewModel.onEvent(SignUpEvent.NameChanged("John Doe"))
        viewModel.onEvent(SignUpEvent.EmailChanged("invalid"))
        viewModel.onEvent(SignUpEvent.PasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.TermsToggled)
        viewModel.onEvent(SignUpEvent.SignUpClicked)
        assertEquals("Invalid email format", viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign up with invalid password shows error`() = runTest {
        viewModel.onEvent(SignUpEvent.NameChanged("John Doe"))
        viewModel.onEvent(SignUpEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(SignUpEvent.PasswordChanged("short"))
        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("short"))
        viewModel.onEvent(SignUpEvent.TermsToggled)
        viewModel.onEvent(SignUpEvent.SignUpClicked)
        assertEquals("Password must be at least 8 characters", viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign up with mismatched passwords shows error`() = runTest {
        viewModel.onEvent(SignUpEvent.NameChanged("John Doe"))
        viewModel.onEvent(SignUpEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(SignUpEvent.PasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password456"))
        viewModel.onEvent(SignUpEvent.TermsToggled)
        viewModel.onEvent(SignUpEvent.SignUpClicked)
        assertEquals("Passwords do not match", viewModel.uiState.value.confirmPasswordError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign up without accepting terms shows general error`() = runTest {
        viewModel.onEvent(SignUpEvent.NameChanged("John Doe"))
        viewModel.onEvent(SignUpEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(SignUpEvent.PasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.SignUpClicked)
        assertEquals("You must accept the Terms and Privacy Policy", viewModel.uiState.value.generalError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign up with taken email shows email error`() = runTest {
        viewModel.onEvent(SignUpEvent.NameChanged("John Doe"))
        viewModel.onEvent(SignUpEvent.EmailChanged("taken@example.com"))
        viewModel.onEvent(SignUpEvent.PasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.TermsToggled)
        viewModel.onEvent(SignUpEvent.SignUpClicked)
        
        assertTrue(viewModel.uiState.value.isLoading)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("Email already in use", viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign up with valid credentials succeeds`() = runTest {
        viewModel.onEvent(SignUpEvent.NameChanged("John Doe"))
        viewModel.onEvent(SignUpEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(SignUpEvent.PasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password123"))
        viewModel.onEvent(SignUpEvent.TermsToggled)
        viewModel.onEvent(SignUpEvent.SignUpClicked)
        
        assertTrue(viewModel.uiState.value.isLoading)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.nameError)
        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
        assertNull(viewModel.uiState.value.confirmPasswordError)
        assertNull(viewModel.uiState.value.generalError)
    }

    @Test
    fun `sign up with repository error shows general error`() = runTest {
        val errorRepository = object : AuthRepository {
            override suspend fun signIn(email: String, password: String): AuthResult {
                return AuthResult.Success
            }
            override suspend fun signUp(name: String, email: String, password: String): AuthResult {
                return AuthResult.Error("Network error")
            }
            override suspend fun requestPasswordReset(email: String): AuthResult {
                return AuthResult.Success
            }
            override suspend fun signOut() {}
        }
        
        val errorViewModel = SignUpViewModel(
            authRepository = errorRepository,
            validateNameUseCase = validateNameUseCase,
            validateEmailUseCase = validateEmailUseCase,
            validatePasswordUseCase = validatePasswordUseCase,
            validateConfirmPasswordUseCase = validateConfirmPasswordUseCase
        )
        
        errorViewModel.onEvent(SignUpEvent.NameChanged("John Doe"))
        errorViewModel.onEvent(SignUpEvent.EmailChanged("test@example.com"))
        errorViewModel.onEvent(SignUpEvent.PasswordChanged("password123"))
        errorViewModel.onEvent(SignUpEvent.ConfirmPasswordChanged("password123"))
        errorViewModel.onEvent(SignUpEvent.TermsToggled)
        errorViewModel.onEvent(SignUpEvent.SignUpClicked)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("Network error", errorViewModel.uiState.value.generalError)
        assertFalse(errorViewModel.uiState.value.isLoading)
    }
}
