package com.example.myapplication.ui.login

import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
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
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var validateEmailUseCase: ValidateEmailUseCase
    private lateinit var validatePasswordUseCase: ValidatePasswordUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        validateEmailUseCase = ValidateEmailUseCase()
        validatePasswordUseCase = ValidatePasswordUseCase()
        viewModel = LoginViewModel(
            authRepository = fakeAuthRepository,
            validateEmailUseCase = validateEmailUseCase,
            validatePasswordUseCase = validatePasswordUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `email change updates state and clears error`() = runTest {
        viewModel.onEvent(LoginEvent.EmailChanged("test@example.com"))
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `password change updates state and clears error`() = runTest {
        viewModel.onEvent(LoginEvent.PasswordChanged("password123"))
        assertEquals("password123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `toggle password visibility updates state`() = runTest {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onEvent(LoginEvent.TogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.onEvent(LoginEvent.TogglePasswordVisibility)
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun `sign in with invalid email shows error`() = runTest {
        viewModel.onEvent(LoginEvent.EmailChanged("invalid"))
        viewModel.onEvent(LoginEvent.PasswordChanged("password123"))
        viewModel.onEvent(LoginEvent.SignInClicked)
        assertEquals("Invalid email format", viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign in with invalid password shows error`() = runTest {
        viewModel.onEvent(LoginEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("short"))
        viewModel.onEvent(LoginEvent.SignInClicked)
        assertEquals("Password must be at least 8 characters", viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign in with blank fields shows errors`() = runTest {
        viewModel.onEvent(LoginEvent.SignInClicked)
        assertEquals("Email is required", viewModel.uiState.value.emailError)
        assertEquals("Password is required", viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `sign in with valid credentials succeeds`() = runTest {
        viewModel.onEvent(LoginEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("password123"))
        viewModel.onEvent(LoginEvent.SignInClicked)
        
        assertTrue(viewModel.uiState.value.isLoading)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
        assertNull(viewModel.uiState.value.generalError)
    }

    @Test
    fun `sign in with repository error shows general error`() = runTest {
        val errorRepository = object : AuthRepository {
            override suspend fun signIn(email: String, password: String): AuthResult {
                return AuthResult.Error("Network error")
            }
        }
        
        val errorViewModel = LoginViewModel(
            authRepository = errorRepository,
            validateEmailUseCase = validateEmailUseCase,
            validatePasswordUseCase = validatePasswordUseCase
        )
        
        errorViewModel.onEvent(LoginEvent.EmailChanged("test@example.com"))
        errorViewModel.onEvent(LoginEvent.PasswordChanged("password123"))
        errorViewModel.onEvent(LoginEvent.SignInClicked)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("Network error", errorViewModel.uiState.value.generalError)
        assertFalse(errorViewModel.uiState.value.isLoading)
    }
}
