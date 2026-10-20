package com.example.myapplication.platform.notification

import com.example.myapplication.domain.model.PermissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter

actual class NotificationPermissionChecker actual constructor() {

    private val _status = MutableStateFlow<PermissionStatus>(PermissionStatus.NotDetermined)
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    init {
        checkPermission()
    }

    actual fun observeStatus(): Flow<PermissionStatus> = _status.asStateFlow()

    actual suspend fun openAppSettings() {
        val settingsUrl = NSURL(string = UIApplication.openSettingsURLString)
        UIApplication.sharedApplication.openURL(settingsUrl!!)
    }

    fun checkPermission() {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = when (settings.authorizationStatus) {
                UNAuthorizationStatusAuthorized -> PermissionStatus.Granted
                UNAuthorizationStatusDenied -> PermissionStatus.Denied
                UNAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
                else -> PermissionStatus.NotDetermined
            }
            _status.value = status
        }
    }

    fun requestPermission() {
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            _status.value = if (granted) PermissionStatus.Granted else PermissionStatus.Denied
        }
    }

    actual fun refreshStatus() {
        checkPermission()
    }
}

actual fun getNotificationPermissionChecker(): NotificationPermissionChecker {
    return NotificationPermissionChecker()
}
