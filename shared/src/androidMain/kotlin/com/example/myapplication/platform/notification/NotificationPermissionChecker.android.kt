package com.example.myapplication.platform.notification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.myapplication.domain.model.PermissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class NotificationPermissionChecker actual constructor() {

    private val _status = MutableStateFlow<PermissionStatus>(PermissionStatus.NotDetermined)
    private var context: Context? = null
    private var hasRequestedPermission = false

    fun setContext(context: Context) {
        this.context = context
        refreshStatus()
    }

    actual fun observeStatus(): Flow<PermissionStatus> = _status.asStateFlow()

    actual suspend fun openAppSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context?.packageName, null)
        }
        context?.startActivity(intent)
    }

    actual fun refreshStatus() {
        _status.value = checkPermission()
    }

    fun onPermissionResult(granted: Boolean) {
        _status.value = if (granted) PermissionStatus.Granted else PermissionStatus.Denied
        hasRequestedPermission = true
    }

    fun shouldRequestPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
               _status.value == PermissionStatus.NotDetermined && 
               !hasRequestedPermission
    }

    private fun checkPermission(): PermissionStatus {
        val ctx = context ?: return PermissionStatus.NotDetermined
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == 
                         android.content.pm.PackageManager.PERMISSION_GRANTED
            if (granted) PermissionStatus.Granted else PermissionStatus.Denied
        } else {
            PermissionStatus.Granted // Always granted below API 33
        }
    }
}

actual fun getNotificationPermissionChecker(): NotificationPermissionChecker {
    return NotificationPermissionChecker()
}
