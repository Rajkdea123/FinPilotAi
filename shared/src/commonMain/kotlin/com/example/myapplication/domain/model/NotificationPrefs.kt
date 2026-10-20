package com.example.myapplication.domain.model

data class NotificationPrefs(
    val spendingAlerts: Boolean = true,
    val unusedSubscriptions: Boolean = true,
    val budgetWarnings: Boolean = true,
    val budgetThresholdPercent: Int = 80,
    val weeklySummary: Boolean = false,
    val syncIssues: Boolean = true
) {
    init {
        require(budgetThresholdPercent in listOf(80, 90, 100)) {
            "budgetThresholdPercent must be 80, 90, or 100"
        }
    }
}
