package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.PermissionStatus
import com.example.myapplication.platform.notification.NotificationPermissionChecker
import kotlinx.coroutines.flow.Flow

class ObservePermissionStatusUseCase(
    private val notificationPermissionChecker: NotificationPermissionChecker
) {
    operator fun invoke(): Flow<PermissionStatus> {
        return notificationPermissionChecker.observeStatus()
    }
}
