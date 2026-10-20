package com.example.myapplication.ui.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.repository.SettingsRepository
import com.example.myapplication.ui.theme.Accent
import com.example.myapplication.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppearanceUiState(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val accent: Accent = Accent.Mint,
    val savedThemeMode: ThemeMode = ThemeMode.Dark,
    val savedAccent: Accent = Accent.Mint,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val navigateBack: Boolean = false
)

sealed interface AppearanceEvent {
    data class ThemeSelected(val themeMode: ThemeMode) : AppearanceEvent
    data class AccentSelected(val accent: Accent) : AppearanceEvent
    object ApplyClicked : AppearanceEvent
    object BackClicked : AppearanceEvent
}

class AppearanceViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppearanceUiState())
    val uiState: StateFlow<AppearanceUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = settingsRepository.observe().firstOrNull() ?: AppSettings()
            _uiState.update {
                it.copy(
                    themeMode = settings.themeMode,
                    accent = settings.accentColor,
                    savedThemeMode = settings.themeMode,
                    savedAccent = settings.accentColor
                )
            }
        }
    }

    fun onEvent(event: AppearanceEvent) {
        when (event) {
            is AppearanceEvent.ThemeSelected -> {
                _uiState.update { it.copy(themeMode = event.themeMode) }
                // Live preview: update settings repository immediately so AppTheme reflects live!
                updateSettingsLive(event.themeMode, _uiState.value.accent)
            }

            is AppearanceEvent.AccentSelected -> {
                _uiState.update { it.copy(accent = event.accent) }
                // Live preview: update settings repository immediately so AppTheme reflects live!
                updateSettingsLive(_uiState.value.themeMode, event.accent)
            }

            AppearanceEvent.ApplyClicked -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSaving = true) }
                    val current = _uiState.value
                    settingsRepository.update {
                        it.copy(
                            themeMode = current.themeMode,
                            accentColor = current.accent
                        )
                    }
                    _uiState.update {
                        it.copy(
                            savedThemeMode = current.themeMode,
                            savedAccent = current.accent,
                            isSaving = false,
                            saveSuccess = true
                        )
                    }
                }
            }

            AppearanceEvent.BackClicked -> {
                viewModelScope.launch {
                    // Revert live preview if not applied
                    val current = _uiState.value
                    if (current.themeMode != current.savedThemeMode || current.accent != current.savedAccent) {
                        settingsRepository.update {
                            it.copy(
                                themeMode = current.savedThemeMode,
                                accentColor = current.savedAccent
                            )
                        }
                    }
                    _uiState.update { it.copy(navigateBack = true) }
                }
            }
        }
    }

    private fun updateSettingsLive(themeMode: ThemeMode, accent: Accent) {
        viewModelScope.launch {
            settingsRepository.update {
                it.copy(
                    themeMode = themeMode,
                    accentColor = accent
                )
            }
        }
    }
}
