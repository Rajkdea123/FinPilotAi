package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.repository.SettingsRepository

class UpdateSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(transform: (AppSettings) -> AppSettings) {
        settingsRepository.update(transform)
    }
}
