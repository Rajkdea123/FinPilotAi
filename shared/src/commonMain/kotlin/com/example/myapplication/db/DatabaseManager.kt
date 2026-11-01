package com.example.myapplication.db

import com.example.myapplication.di.core.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DatabaseManager(
    private val databaseDriverFactory: DatabaseDriverFactory,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock
) {
    private var database: FinPilotDatabase? = null
    private var seeded = false

    suspend fun getDatabase(): FinPilotDatabase = withContext(dispatchers.io) {
        if (database == null) {
            val driver = databaseDriverFactory.createDriver()
            database = FinPilotDatabase(driver)
            
            // Seed database in debug builds
            if (!seeded) {
                seedDatabase(database!!, dispatchers, clock)
                seeded = true
            }
        }
        database!!
    }

    suspend fun close() = withContext(dispatchers.io) {
        database = null
        seeded = false
    }

    suspend fun clearDatabase() = withContext(dispatchers.io) {
        val db = database ?: return@withContext
        db.transaction {
            db.expenseQueries.deleteAllExpenses()
            db.budgetQueries.deleteAllBudgets()
            db.insightQueries.deleteAllInsights()
            db.subscriptionQueries.deleteAllSubscriptions()
            db.chatMessageQueries.deleteAllChatMessages()
            db.userProfileQueries.deleteAllUserProfiles()
            db.syncOutboxQueries.deleteAllSyncOutbox()
        }
    }
}
