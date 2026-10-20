package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.NotificationPrefs
import com.example.myapplication.domain.repository.NotificationPrefsRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotificationPrefsUseCase(
    private val notificationPrefsRepository: NotificationPrefsRepository
) {
    operator fun invoke(): Flow<NotificationPrefs> {
        return notificationPrefsRepository.observe()
    }
}
