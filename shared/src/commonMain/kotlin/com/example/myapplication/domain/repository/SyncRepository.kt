package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.model.SyncConflict
import com.example.myapplication.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun observeStatus(): Flow<SyncStatus>
    fun observeConflicts(): Flow<List<SyncConflict>>
    suspend fun syncNow()
    suspend fun resolveConflict(conflictId: String, choice: com.example.myapplication.domain.model.ConflictChoice)
}

interface SettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
