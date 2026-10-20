package com.example.myapplication.ui.sync

import com.example.myapplication.data.repository.FakeSettingsRepository
import com.example.myapplication.data.repository.FakeSyncRepository
import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.model.ConflictChoice
import com.example.myapplication.domain.model.SyncStatus
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.repository.SyncRepository
import com.example.myapplication.domain.usecase.ObserveSyncStatusUseCase
import com.example.myapplication.domain.usecase.ResolveConflictUseCase
import com.example.myapplication.domain.usecase.SyncNowUseCase
import com.example.myapplication.domain.usecase.UpdateSettingsUseCase
import com.example.myapplication.domain.platform.biometric.BiometricAuthenticator
import com.example.myapplication.platform.network.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {

    private lateinit var fakeSyncRepository: FakeSyncRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var observeSyncStatusUseCase: ObserveSyncStatusUseCase
    private lateinit var resolveConflictUseCase: ResolveConflictUseCase
    private lateinit var syncNowUseCase: SyncNowUseCase
    private lateinit var updateSettingsUseCase: UpdateSettingsUseCase
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var biometricAuthenticator: BiometricAuthenticator
    private lateinit var viewModel: SyncViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeSyncRepository = FakeSyncRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        observeSyncStatusUseCase = ObserveSyncStatusUseCase(fakeSyncRepository)
        resolveConflictUseCase = ResolveConflictUseCase(fakeSyncRepository)
        syncNowUseCase = SyncNowUseCase(fakeSyncRepository)
        updateSettingsUseCase = UpdateSettingsUseCase(fakeSettingsRepository)
        networkMonitor = object : NetworkMonitor {
            override fun observe(): Flow<Boolean> = flowOf(true)
        }
        biometricAuthenticator = object : BiometricAuthenticator {
            override suspend fun authenticate(): Boolean = true
        }
        viewModel = SyncViewModel(
            observeSyncStatusUseCase,
            resolveConflictUseCase,
            syncNowUseCase,
            updateSettingsUseCase,
            networkMonitor,
            biometricAuthenticator,
            fakeSettingsRepository
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads status and settings`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.status)
        assertNotNull(state.settings)
        assertTrue(state.isOnline)
        assertNull(state.error)
    }

    @Test
    fun `offline to online with autoSync triggers sync`() = runTest {
        val offlineNetworkMonitor = object : NetworkMonitor {
            override fun observe(): Flow<Boolean> = flowOf(false)
        }

        val offlineViewModel = SyncViewModel(
            observeSyncStatusUseCase,
            resolveConflictUseCase,
            syncNowUseCase,
            updateSettingsUseCase,
            offlineNetworkMonitor,
            biometricAuthenticator,
            fakeSettingsRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(offlineViewModel.uiState.value.isOnline)

        // Now simulate coming online
        val onlineNetworkMonitor = object : NetworkMonitor {
            override fun observe(): Flow<Boolean> = flowOf(true)
        }

        val onlineViewModel = SyncViewModel(
            observeSyncStatusUseCase,
            resolveConflictUseCase,
            syncNowUseCase,
            updateSettingsUseCase,
            onlineNetworkMonitor,
            biometricAuthenticator,
            fakeSettingsRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(onlineViewModel.uiState.value.isOnline)
    }

    @Test
    fun `conflict selection updates selected choice`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val conflict = viewModel.uiState.value.conflicts.first()
        viewModel.onEvent(SyncEvent.SelectConflictChoice(conflict.id, ConflictChoice.Local))

        val state = viewModel.uiState.value
        assertEquals(ConflictChoice.Local, state.selectedConflictChoice[conflict.id])
    }

    @Test
    fun `biometric toggle denied when authentication fails`() = runTest {
        val failingBiometric = object : BiometricAuthenticator {
            override suspend fun authenticate(): Boolean = false
        }

        val failingViewModel = SyncViewModel(
            observeSyncStatusUseCase,
            resolveConflictUseCase,
            syncNowUseCase,
            updateSettingsUseCase,
            networkMonitor,
            failingBiometric,
            fakeSettingsRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val initialState = failingViewModel.uiState.value.settings.biometricLock
        assertFalse(initialState)

        failingViewModel.onEvent(SyncEvent.ToggleBiometricLock)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = failingViewModel.uiState.value
        assertFalse(state.settings.biometricLock)
        assertNotNull(state.biometricError)
    }

    @Test
    fun `settings update toggles boolean values`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val initialAutoSync = viewModel.uiState.value.settings.autoSyncOnWifi
        viewModel.onEvent(SyncEvent.ToggleAutoSync)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(!initialAutoSync, state.settings.autoSyncOnWifi)
    }
}
