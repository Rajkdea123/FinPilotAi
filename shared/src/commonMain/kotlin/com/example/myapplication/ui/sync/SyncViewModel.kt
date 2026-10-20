package com.example.myapplication.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.model.ConflictChoice
import com.example.myapplication.domain.model.SyncStatus
import com.example.myapplication.platform.biometric.BiometricAuthenticator
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.repository.SyncRepository
import com.example.myapplication.domain.usecase.ObserveSyncStatusUseCase
import com.example.myapplication.domain.usecase.ResolveConflictUseCase
import com.example.myapplication.domain.usecase.SyncNowUseCase
import com.example.myapplication.domain.usecase.UpdateSettingsUseCase
import com.example.myapplication.platform.network.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SyncUiState(
    val status: SyncStatus? = null,
    val conflicts: List<com.example.myapplication.domain.model.SyncConflict> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isOnline: Boolean = true,
    val error: String? = null,
    val selectedConflictChoice: Map<String, ConflictChoice> = emptyMap(),
    val biometricError: String? = null
)

sealed interface SyncEvent {
    object SyncNowClicked : SyncEvent
    data class ResolveConflict(val conflictId: String, val choice: ConflictChoice) : SyncEvent
    data class SelectConflictChoice(val conflictId: String, val choice: ConflictChoice) : SyncEvent
    object ToggleAutoSync : SyncEvent
    object ToggleSmartNotifications : SyncEvent
    object ToggleBiometricLock : SyncEvent
    object BackClicked : SyncEvent
    object ClearBiometricError : SyncEvent
}

class SyncViewModel(
    private val observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val resolveConflictUseCase: ResolveConflictUseCase,
    private val syncNowUseCase: SyncNowUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val networkMonitor: NetworkMonitor,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private var syncJob: Job? = null
    private var hasTriggeredAutoSync = false

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                observeSyncStatusUseCase(),
                networkMonitor.observe(),
                settingsRepository.observe()
            ) { status, isOnline, settings ->
                Triple(status, isOnline, settings)
            }.catch { cause ->
                _uiState.update {
                    it.copy(error = cause.message ?: "Failed to load sync status")
                }
            }.collect { (status, isOnline, settings) ->
                _uiState.update {
                    it.copy(
                        status = status,
                        isOnline = isOnline,
                        settings = settings,
                        error = null
                    )
                }

                // Trigger auto-sync when coming online with auto-sync enabled
                if (isOnline && !hasTriggeredAutoSync && settings.autoSyncOnWifi) {
                    hasTriggeredAutoSync = true
                    syncNow()
                }
            }
        }
    }

    fun onEvent(event: SyncEvent) {
        when (event) {
            SyncEvent.SyncNowClicked -> {
                syncNow()
            }

            is SyncEvent.ResolveConflict -> {
                viewModelScope.launch {
                    resolveConflictUseCase(event.conflictId, event.choice)
                    _uiState.update {
                        it.copy(
                            selectedConflictChoice = it.selectedConflictChoice - event.conflictId
                        )
                    }
                }
            }

            is SyncEvent.SelectConflictChoice -> {
                _uiState.update {
                    it.copy(
                        selectedConflictChoice = it.selectedConflictChoice + (event.conflictId to event.choice)
                    )
                }
            }

            SyncEvent.ToggleAutoSync -> {
                viewModelScope.launch {
                    updateSettingsUseCase { it.copy(autoSyncOnWifi = !it.autoSyncOnWifi) }
                    _uiState.update {
                        it.copy(settings = it.settings.copy(autoSyncOnWifi = !it.settings.autoSyncOnWifi))
                    }
                }
            }

            SyncEvent.ToggleSmartNotifications -> {
                viewModelScope.launch {
                    updateSettingsUseCase { it.copy(smartNotifications = !it.smartNotifications) }
                    _uiState.update {
                        it.copy(settings = it.settings.copy(smartNotifications = !it.settings.smartNotifications))
                    }
                }
            }

            SyncEvent.ToggleBiometricLock -> {
                val currentState = _uiState.value.settings.biometricLock
                if (!currentState) {
                    // Turning on - need biometric authentication
                    viewModelScope.launch {
                        val result = biometricAuthenticator.authenticate()
                        if (result is com.example.myapplication.platform.biometric.BiometricResult.Success) {
                            updateSettingsUseCase { it.copy(biometricLock = true) }
                            _uiState.update {
                                it.copy(settings = it.settings.copy(biometricLock = true))
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    biometricError = "Biometric lock was not enabled"
                                )
                            }
                        }
                    }
                } else {
                    // Turning off - no check needed
                    viewModelScope.launch {
                        updateSettingsUseCase { it.copy(biometricLock = false) }
                        _uiState.update {
                            it.copy(settings = it.settings.copy(biometricLock = false))
                        }
                    }
                }
            }

            SyncEvent.BackClicked -> {
                // Handled by navigation
            }

            SyncEvent.ClearBiometricError -> {
                _uiState.update {
                    it.copy(biometricError = null)
                }
            }
        }
    }

    private fun syncNow() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            syncNowUseCase()
        }
    }
}
