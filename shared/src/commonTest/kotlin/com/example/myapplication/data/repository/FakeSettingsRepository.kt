package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository : SettingsRepository {

    private val _settings = MutableStateFlow(
        AppSettings(
            autoSyncOnWifi = true,
            smartNotifications = true,
            biometricLock = false,
            themeMode = com.example.myapplication.ui.theme.ThemeMode.System
        )
    )

    // TODO: Replace with multiplatform-settings

    override fun observe(): Flow<AppSettings> {
        return _settings
    }

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        _settings.value = transform(_settings.value)
    }
}
