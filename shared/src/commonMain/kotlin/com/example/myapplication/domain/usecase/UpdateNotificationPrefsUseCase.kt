package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.NotificationPrefs
import com.example.myapplication.domain.repository.NotificationPrefsRepository

class UpdateNotificationPrefsUseCase(
    private val notificationPrefsRepository: NotificationPrefsRepository
) {
    suspend operator fun invoke(transform: (NotificationPrefs) -> NotificationPrefs) {
        notificationPrefsRepository.update(transform)
    }
}
