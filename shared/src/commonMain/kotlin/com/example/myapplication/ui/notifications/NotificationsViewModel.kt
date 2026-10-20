package com.example.myapplication.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.NotificationPrefs
import com.example.myapplication.domain.model.PermissionStatus
import com.example.myapplication.domain.repository.NotificationPrefsRepository
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.usecase.ObserveNotificationPrefsUseCase
import com.example.myapplication.domain.usecase.ObservePermissionStatusUseCase
import com.example.myapplication.domain.usecase.UpdateNotificationPrefsUseCase
import com.example.myapplication.domain.usecase.UpdateSettingsUseCase
import com.example.myapplication.platform.notification.NotificationPermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val masterEnabled: Boolean = true,
    val prefs: NotificationPrefs = NotificationPrefs(),
    val permissionStatus: PermissionStatus = PermissionStatus.Granted,
    val showBudgetThreshold: Boolean = false,
    val error: String? = null
)

sealed interface NotificationsEvent {
    object MasterToggled : NotificationsEvent
    object SpendingAlertsToggled : NotificationsEvent
    object UnusedSubscriptionsToggled : NotificationsEvent
    object BudgetWarningsToggled : NotificationsEvent
    data class ThresholdSelected(val percent: Int) : NotificationsEvent
    object WeeklySummaryToggled : NotificationsEvent
    object SyncIssuesToggled : NotificationsEvent
    object OpenSystemSettingsClicked : NotificationsEvent
    object RequestPermissionClicked : NotificationsEvent
    object BackClicked : NotificationsEvent
    object ToggleBudgetThreshold : NotificationsEvent
}

class NotificationsViewModel(
    private val observeNotificationPrefsUseCase: ObserveNotificationPrefsUseCase,
    private val updateNotificationPrefsUseCase: UpdateNotificationPrefsUseCase,
    private val observePermissionStatusUseCase: ObservePermissionStatusUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val notificationPermissionChecker: NotificationPermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                settingsRepository.observe(),
                observeNotificationPrefsUseCase(),
                observePermissionStatusUseCase()
            ) { settings, prefs, permissionStatus ->
                Triple(settings, prefs, permissionStatus)
            }.catch { cause ->
                _uiState.update {
                    it.copy(error = cause.message ?: "Failed to load notification settings")
                }
            }.collect { (settings, prefs, permissionStatus) ->
                _uiState.update {
                    it.copy(
                        masterEnabled = settings.smartNotifications,
                        prefs = prefs,
                        permissionStatus = permissionStatus,
                        showBudgetThreshold = prefs.budgetWarnings,
                        error = null
                    )
                }
            }
        }
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.MasterToggled -> {
                val currentMasterEnabled = _uiState.value.masterEnabled
                val newMasterEnabled = !currentMasterEnabled
                
                viewModelScope.launch {
                    updateSettingsUseCase { it.copy(smartNotifications = newMasterEnabled) }
                    _uiState.update {
                        it.copy(masterEnabled = newMasterEnabled)
                    }
                    
                    // If turning on and permission is NotDetermined, request permission
                    if (newMasterEnabled && _uiState.value.permissionStatus == PermissionStatus.NotDetermined) {
                        requestPermission()
                    }
                }
            }

            NotificationsEvent.SpendingAlertsToggled -> {
                viewModelScope.launch {
                    updateNotificationPrefsUseCase { it.copy(spendingAlerts = !it.spendingAlerts) }
                    _uiState.update {
                        it.copy(prefs = it.prefs.copy(spendingAlerts = !it.prefs.spendingAlerts))
                    }
                }
            }

            NotificationsEvent.UnusedSubscriptionsToggled -> {
                viewModelScope.launch {
                    updateNotificationPrefsUseCase { it.copy(unusedSubscriptions = !it.unusedSubscriptions) }
                    _uiState.update {
                        it.copy(prefs = it.prefs.copy(unusedSubscriptions = !it.prefs.unusedSubscriptions))
                    }
                }
            }

            NotificationsEvent.BudgetWarningsToggled -> {
                viewModelScope.launch {
                    updateNotificationPrefsUseCase { it.copy(budgetWarnings = !it.budgetWarnings) }
                    _uiState.update {
                        it.copy(
                            prefs = it.prefs.copy(budgetWarnings = !it.prefs.budgetWarnings),
                            showBudgetThreshold = !it.prefs.budgetWarnings
                        )
                    }
                }
            }

            is NotificationsEvent.ThresholdSelected -> {
                viewModelScope.launch {
                    updateNotificationPrefsUseCase { it.copy(budgetThresholdPercent = event.percent) }
                    _uiState.update {
                        it.copy(prefs = it.prefs.copy(budgetThresholdPercent = event.percent))
                    }
                }
            }

            NotificationsEvent.WeeklySummaryToggled -> {
                viewModelScope.launch {
                    updateNotificationPrefsUseCase { it.copy(weeklySummary = !it.weeklySummary) }
                    _uiState.update {
                        it.copy(prefs = it.prefs.copy(weeklySummary = !it.prefs.weeklySummary))
                    }
                }
            }

            NotificationsEvent.SyncIssuesToggled -> {
                viewModelScope.launch {
                    updateNotificationPrefsUseCase { it.copy(syncIssues = !it.syncIssues) }
                    _uiState.update {
                        it.copy(prefs = it.prefs.copy(syncIssues = !it.prefs.syncIssues))
                    }
                }
            }

            NotificationsEvent.OpenSystemSettingsClicked -> {
                viewModelScope.launch {
                    notificationPermissionChecker.openAppSettings()
                }
            }

            NotificationsEvent.RequestPermissionClicked -> {
                requestPermission()
            }

            NotificationsEvent.BackClicked -> {
                // Handled by navigation
            }

            NotificationsEvent.ToggleBudgetThreshold -> {
                _uiState.update {
                    it.copy(showBudgetThreshold = !it.showBudgetThreshold)
                }
            }
        }
    }

    private fun requestPermission() {
        // Platform-specific implementation will handle the actual permission request
        // This is a placeholder for the permission request logic
        viewModelScope.launch {
            // The actual permission request will be handled in the UI layer
            // using platform-specific APIs
        }
    }

    fun refreshPermissionStatus() {
        notificationPermissionChecker.refreshStatus()
    }
}
