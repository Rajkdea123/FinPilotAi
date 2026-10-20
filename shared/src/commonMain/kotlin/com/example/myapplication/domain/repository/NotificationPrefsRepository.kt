package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.NotificationPrefs
import kotlinx.coroutines.flow.Flow

interface NotificationPrefsRepository {
    fun observe(): Flow<NotificationPrefs>
    suspend fun update(transform: (NotificationPrefs) -> NotificationPrefs)
}
