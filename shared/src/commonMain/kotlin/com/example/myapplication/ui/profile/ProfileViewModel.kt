package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.model.ProfileStats
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.domain.usecase.GetProfileStatsUseCase
import com.example.myapplication.domain.usecase.GetProfileUseCase
import com.example.myapplication.domain.usecase.SignOutUseCase
import com.example.myapplication.platform.storage.UserPreferencesStorage
import com.example.myapplication.ui.theme.ThemeMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val stats: ProfileStats? = null,
    val themeLabel: String = "System",
    val isLoading: Boolean = true,
    val error: String? = null,
    val showSignOutDialog: Boolean = false
)

sealed interface ProfileEvent {
    object EditClicked : ProfileEvent
    object BudgetClicked : ProfileEvent
    object DevicesClicked : ProfileEvent
    object NotificationsClicked : ProfileEvent
    object AppearanceClicked : ProfileEvent
    object SignOutClicked : ProfileEvent
    object SignOutConfirmed : ProfileEvent
    object SignOutDismissed : ProfileEvent
    object Retry : ProfileEvent
}

sealed interface ProfileNavigationEvent {
    object NavigateToEditProfile : ProfileNavigationEvent
    object NavigateToBudget : ProfileNavigationEvent
    object NavigateToDevices : ProfileNavigationEvent
    object NavigateToNotifications : ProfileNavigationEvent
    object NavigateToAppearance : ProfileNavigationEvent
    object NavigateToLogin : ProfileNavigationEvent
}

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val getProfileStatsUseCase: GetProfileStatsUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel(), KoinComponent {
    private val userPreferencesStorage: UserPreferencesStorage by inject()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<ProfileNavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getProfileUseCase(),
                getProfileStatsUseCase(),
                settingsRepository.observe()
            ) { profile, stats, settings ->
                Triple(profile, stats, settings)
            }.catch { cause ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = cause.message ?: "Failed to load profile"
                    )
                }
            }.collect { (profile, stats, settings) ->
                _uiState.update {
                    it.copy(
                        profile = profile,
                        stats = stats,
                        themeLabel = settings.themeMode.toLabel(),
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.EditClicked -> {
                viewModelScope.launch {
                    _navigationEvents.send(ProfileNavigationEvent.NavigateToEditProfile)
                }
            }

            ProfileEvent.BudgetClicked -> {
                viewModelScope.launch {
                    _navigationEvents.send(ProfileNavigationEvent.NavigateToBudget)
                }
            }

            ProfileEvent.DevicesClicked -> {
                viewModelScope.launch {
                    _navigationEvents.send(ProfileNavigationEvent.NavigateToDevices)
                }
            }

            ProfileEvent.NotificationsClicked -> {
                viewModelScope.launch {
                    _navigationEvents.send(ProfileNavigationEvent.NavigateToNotifications)
                }
            }

            ProfileEvent.AppearanceClicked -> {
                viewModelScope.launch {
                    _navigationEvents.send(ProfileNavigationEvent.NavigateToAppearance)
                }
            }

            ProfileEvent.SignOutClicked -> {
                _uiState.update { it.copy(showSignOutDialog = true) }
            }

            ProfileEvent.SignOutConfirmed -> {
                viewModelScope.launch {
                    signOutUseCase()
                    userPreferencesStorage.clearAll()
                    _uiState.update { it.copy(showSignOutDialog = false) }
                    _navigationEvents.send(ProfileNavigationEvent.NavigateToLogin)
                }
            }

            ProfileEvent.SignOutDismissed -> {
                _uiState.update { it.copy(showSignOutDialog = false) }
            }

            ProfileEvent.Retry -> {
                _uiState.update { it.copy(isLoading = true, error = null) }
                observeData()
            }
        }
    }

    private fun ThemeMode.toLabel(): String {
        return when (this) {
            ThemeMode.System -> "System"
            ThemeMode.Light -> "Light"
            ThemeMode.Dark -> "Dark"
        }
    }
}
