package com.example.myapplication.ui.profile

import com.example.myapplication.data.repository.FakeAuthRepository
import com.example.myapplication.data.repository.FakeProfileRepository
import com.example.myapplication.data.repository.FakeSettingsRepository
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.ProfileRepository
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.usecase.GetProfileStatsUseCase
import com.example.myapplication.domain.usecase.GetProfileUseCase
import com.example.myapplication.domain.usecase.SignOutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var getProfileUseCase: GetProfileUseCase
    private lateinit var getProfileStatsUseCase: GetProfileStatsUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeProfileRepository = FakeProfileRepository()
        fakeAuthRepository = FakeAuthRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        getProfileUseCase = GetProfileUseCase(fakeProfileRepository)
        getProfileStatsUseCase = GetProfileStatsUseCase(fakeProfileRepository)
        signOutUseCase = SignOutUseCase(fakeAuthRepository)
        viewModel = ProfileViewModel(
            getProfileUseCase,
            getProfileStatsUseCase,
            signOutUseCase,
            fakeSettingsRepository
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads profile and stats`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.profile)
        assertNotNull(state.stats)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("System", state.themeLabel)
    }

    @Test
    fun `edit clicked emits navigation event`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.EditClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.navigationEvents.first()
        assertTrue(event is ProfileNavigationEvent.NavigateToEditProfile)
    }

    @Test
    fun `budget clicked emits navigation event`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.BudgetClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.navigationEvents.first()
        assertTrue(event is ProfileNavigationEvent.NavigateToBudget)
    }

    @Test
    fun `devices clicked emits navigation event`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.DevicesClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.navigationEvents.first()
        assertTrue(event is ProfileNavigationEvent.NavigateToDevices)
    }

    @Test
    fun `notifications clicked emits navigation event`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.NotificationsClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.navigationEvents.first()
        assertTrue(event is ProfileNavigationEvent.NavigateToNotifications)
    }

    @Test
    fun `appearance clicked emits navigation event`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.AppearanceClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.navigationEvents.first()
        assertTrue(event is ProfileNavigationEvent.NavigateToAppearance)
    }

    @Test
    fun `sign out clicked shows dialog`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.SignOutClicked)

        val state = viewModel.uiState.value
        assertTrue(state.showSignOutDialog)
    }

    @Test
    fun `sign out dismissed hides dialog`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.SignOutClicked)
        viewModel.onEvent(ProfileEvent.SignOutDismissed)

        val state = viewModel.uiState.value
        assertFalse(state.showSignOutDialog)
    }

    @Test
    fun `sign out confirmed emits login navigation`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.SignOutClicked)
        viewModel.onEvent(ProfileEvent.SignOutConfirmed)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showSignOutDialog)

        val event = viewModel.navigationEvents.first()
        assertTrue(event is ProfileNavigationEvent.NavigateToLogin)
    }

    @Test
    fun `retry reloads data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileEvent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.profile)
        assertNotNull(state.stats)
        assertFalse(state.isLoading)
    }
}
