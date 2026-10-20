package com.example.myapplication.domain.model

import kotlinx.datetime.Instant

data class SyncMetadata(
    val id: String,
    val version: Long,
    val updatedAt: Long,
    val deviceId: String
)

sealed class SyncStatus {
    data class Offline(val pendingCount: Int) : SyncStatus()
    data class Syncing(val pendingCount: Int) : SyncStatus()
    data class Synced(val lastSyncedAt: Instant) : SyncStatus()
    data class Error(val message: String, val pendingCount: Int) : SyncStatus()
}

data class ConflictVersion(
    val deviceLabel: String,
    val amountPaise: Long,
    val metadata: SyncMetadata
)

data class SyncConflict(
    val id: String,
    val expenseId: String,
    val local: ConflictVersion,
    val remote: ConflictVersion
)

enum class ConflictChoice {
    Local,
    Remote,
    Latest
}

data class AppSettings(
    val autoSyncOnWifi: Boolean = true,
    val smartNotifications: Boolean = true,
    val biometricLock: Boolean = false,
    val themeMode: com.example.myapplication.ui.theme.ThemeMode = com.example.myapplication.ui.theme.ThemeMode.System,
    val accentColor: com.example.myapplication.ui.theme.Accent = com.example.myapplication.ui.theme.Accent.Mint
)
