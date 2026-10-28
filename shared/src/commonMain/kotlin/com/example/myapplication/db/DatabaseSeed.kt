package com.example.myapplication.db

import com.example.myapplication.di.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

suspend fun seedDatabase(database: FinPilotDatabase, dispatchers: AppCoroutineDispatchers, clock: Clock) {
    if (!isDebugBuild()) return
    
    withContext(dispatchers.io) {
        database.transaction {
            // Check if already seeded
            val existingExpenses = database.expenseQueries.selectAllExpenses().executeAsOneOrNull()
            if (existingExpenses != null) return@transaction

            // Seed sample expenses
            @OptIn(ExperimentalUuidApi::class)
            repeat(10) { index ->
                val id = Uuid.random().toString()
                database.expenseQueries.insertExpense(
                    id = id,
                    amount = (1000 + index * 500) * 100, // Amount in paise
                    category = listOf("Food", "Shopping", "Transport", "Bills", "Entertainment")[index % 5],
                    paymentMethod = listOf("UPI", "Card", "Cash")[index % 3],
                    date = clock.now().toEpochMilliseconds() - (index * 86400000L), // Past days
                    description = "Sample expense $index",
                    version = 1,
                    updatedAt = clock.now().toEpochMilliseconds(),
                    deviceId = "device-1",
                    isDeleted = 0,
                    syncState = "SYNCED"
                )
            }

            // Seed sample budget
            @OptIn(ExperimentalUuidApi::class)
            val budgetId = Uuid.random().toString()
            database.budgetQueries.insertBudget(
                id = budgetId,
                total = 500000L, // 5000 rupees
                category = "TOTAL",
                budgetLimit = 500000L,
                version = 1,
                updatedAt = clock.now().toEpochMilliseconds(),
                deviceId = "device-1",
                isDeleted = 0,
                syncState = "SYNCED"
            )

            // Seed sample subscriptions
            @OptIn(ExperimentalUuidApi::class)
            repeat(3) { index ->
                val subId = Uuid.random().toString()
                database.subscriptionQueries.insertSubscription(
                    id = subId,
                    name = listOf("Netflix", "Spotify", "Amazon Prime")[index],
                    amount = (500 + index * 200) * 100,
                    category = "Entertainment",
                    billingCycle = "MONTHLY",
                    nextBillingDate = clock.now().toEpochMilliseconds() + (30 * 86400000L),
                    isActive = 1,
                    version = 1,
                    updatedAt = clock.now().toEpochMilliseconds(),
                    deviceId = "device-1",
                    isDeleted = 0,
                    syncState = "SYNCED"
                )
            }

            // Seed sample insights
            @OptIn(ExperimentalUuidApi::class)
            repeat(2) { index ->
                val insightId = Uuid.random().toString()
                database.insightQueries.insertInsight(
                    id = insightId,
                    title = "Spending Alert",
                    content = "You've spent 80% of your budget this month",
                    category = "SpendingAlert",
                    date = clock.now().toEpochMilliseconds(),
                    version = 1,
                    updatedAt = clock.now().toEpochMilliseconds(),
                    deviceId = "device-1",
                    isDeleted = 0,
                    syncState = "SYNCED"
                )
            }

            // Seed sample chat messages
            @OptIn(ExperimentalUuidApi::class)
            val chatId = Uuid.random().toString()
            database.chatMessageQueries.insertChatMessage(
                id = chatId,
                role = "ASSISTANT",
                content = "Hello! I'm your finance assistant. How can I help you today?",
                timestamp = clock.now().toEpochMilliseconds(),
                version = 1,
                updatedAt = clock.now().toEpochMilliseconds(),
                deviceId = "device-1",
                isDeleted = 0,
                syncState = "SYNCED"
            )

            // Seed sample user profile
            @OptIn(ExperimentalUuidApi::class)
            val profileId = "current"
            database.userProfileQueries.insertUserProfile(
                id = profileId,
                name = "Test User",
                email = "test@example.com",
                avatarUrl = null,
                version = 1,
                updatedAt = clock.now().toEpochMilliseconds(),
                deviceId = "device-1",
                isDeleted = 0,
                syncState = "SYNCED"
            )
        }
    }
}
