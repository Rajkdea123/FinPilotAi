package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.NotificationPrefs
import com.example.myapplication.domain.repository.NotificationPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNotificationPrefsRepository : NotificationPrefsRepository {
    // TODO: Persist with multiplatform-settings
    // TODO: Backend reads these preferences when generating insights

    private val _prefs = MutableStateFlow(
        NotificationPrefs(
            spendingAlerts = true,
            unusedSubscriptions = true,
            budgetWarnings = true,
            budgetThresholdPercent = 80,
            weeklySummary = false,
            syncIssues = true
        )
    )

    override fun observe(): Flow<NotificationPrefs> {
        return _prefs
    }

    override suspend fun update(transform: (NotificationPrefs) -> NotificationPrefs) {
        _prefs.value = transform(_prefs.value)
    }
}
