package com.example.myapplication.ui.auth

import com.example.myapplication.data.repository.FakeAuthRepository
import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
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
class ForgotPasswordViewModelTest {
    private lateinit var viewModel: ForgotPasswordViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var validateEmailUseCase: ValidateEmailUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        validateEmailUseCase = ValidateEmailUseCase()
        viewModel = ForgotPasswordViewModel(
            authRepository = fakeAuthRepository,
            validateEmailUseCase = validateEmailUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `email change updates state and clears error`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("test@example.com"))
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `send reset link with invalid email shows error`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("invalid"))
        viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        assertEquals("Invalid email format", viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(ForgotPasswordStep.Form, viewModel.uiState.value.step)
    }

    @Test
    fun `send reset link with blank email shows error`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        assertEquals("Email is required", viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(ForgotPasswordStep.Form, viewModel.uiState.value.step)
    }

    @Test
    fun `send reset link with valid email moves to sent step`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        
        assertTrue(viewModel.uiState.value.isLoading)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(ForgotPasswordStep.Sent, viewModel.uiState.value.step)
        assertEquals(30, viewModel.uiState.value.resendSecondsLeft)
    }

    @Test
    fun `send reset link with repository error still moves to sent step for privacy`() = runTest {
        val errorRepository = object : AuthRepository {
            override suspend fun signIn(email: String, password: String): AuthResult {
                return AuthResult.Success
            }
            override suspend fun signUp(name: String, email: String, password: String): AuthResult {
                return AuthResult.Success
            }
            override suspend fun requestPasswordReset(email: String): AuthResult {
                return AuthResult.Error("Network error")
            }
            override suspend fun signOut() {}
        }
        
        val errorViewModel = ForgotPasswordViewModel(
            authRepository = errorRepository,
            validateEmailUseCase = validateEmailUseCase
        )
        
        errorViewModel.onEvent(ForgotPasswordEvent.EmailChanged("test@example.com"))
        errorViewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(ForgotPasswordStep.Sent, errorViewModel.uiState.value.step)
        assertEquals(30, errorViewModel.uiState.value.resendSecondsLeft)
    }

    @Test
    fun `countdown decrements every second`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(30, viewModel.uiState.value.resendSecondsLeft)
        
        testDispatcher.scheduler.advanceTimeBy(1000)
        assertEquals(29, viewModel.uiState.value.resendSecondsLeft)
        
        testDispatcher.scheduler.advanceTimeBy(5000)
        assertEquals(24, viewModel.uiState.value.resendSecondsLeft)
    }

    @Test
    fun `countdown reaches zero after 30 seconds`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        
        testDispatcher.scheduler.advanceTimeBy(30000)
        assertEquals(0, viewModel.uiState.value.resendSecondsLeft)
    }

    @Test
    fun `resend disabled during countdown`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(30, viewModel.uiState.value.resendSecondsLeft)
        
        viewModel.onEvent(ForgotPasswordEvent.ResendClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Should not reset countdown
        assertEquals(30, viewModel.uiState.value.resendSecondsLeft)
    }

    @Test
    fun `resend enabled after countdown reaches zero`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        
        testDispatcher.scheduler.advanceTimeBy(30000)
        assertEquals(0, viewModel.uiState.value.resendSecondsLeft)
        
        viewModel.onEvent(ForgotPasswordEvent.ResendClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Should reset countdown
        assertEquals(30, viewModel.uiState.value.resendSecondsLeft)
    }

    @Test
    fun `resend sends new reset link`() = runTest {
        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("test@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        
        testDispatcher.scheduler.advanceTimeBy(30000)
        assertEquals(0, viewModel.uiState.value.resendSecondsLeft)
        
        viewModel.onEvent(ForgotPasswordEvent.ResendClicked)
        assertTrue(viewModel.uiState.value.isLoading)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(30, viewModel.uiState.value.resendSecondsLeft)
    }
}
