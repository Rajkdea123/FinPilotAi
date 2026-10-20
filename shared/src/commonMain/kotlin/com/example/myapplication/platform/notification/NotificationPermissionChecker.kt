package com.example.myapplication.platform.notification

import com.example.myapplication.domain.model.PermissionStatus
import kotlinx.coroutines.flow.Flow

expect class NotificationPermissionChecker {
    fun observeStatus(): Flow<PermissionStatus>
    suspend fun openAppSettings()
    fun refreshStatus()
}

expect fun getNotificationPermissionChecker(): NotificationPermissionChecker
