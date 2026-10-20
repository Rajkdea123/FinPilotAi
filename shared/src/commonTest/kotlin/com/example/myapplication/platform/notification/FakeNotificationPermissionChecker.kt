package com.example.myapplication.platform.notification

import com.example.myapplication.domain.model.PermissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNotificationPermissionChecker : NotificationPermissionChecker {

    private val _status = MutableStateFlow<PermissionStatus>(PermissionStatus.Granted)

    override fun observeStatus(): Flow<PermissionStatus> = _status.asStateFlow()

    override suspend fun openAppSettings() {
        // No-op for tests
    }

    override fun refreshStatus() {
        // No-op for tests
    }

    fun setStatus(status: PermissionStatus) {
        _status.value = status
    }
}
