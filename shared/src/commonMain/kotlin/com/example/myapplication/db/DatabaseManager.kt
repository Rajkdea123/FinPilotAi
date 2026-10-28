package com.example.myapplication.db

import app.cash.sqldelight.db.SqlDriver
import com.example.myapplication.di.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DatabaseManager(
    private val driverFactory: DatabaseDriverFactory,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock
) {
    private var driver: SqlDriver? = null
    private var database: FinPilotDatabase? = null
    private var seeded = false

    suspend fun getDatabase(): FinPilotDatabase = withContext(dispatchers.io) {
        if (database == null) {
            driver = driverFactory.createDriver()
            database = FinPilotDatabase(driver!!)
            
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
        driver?.close()
        driver = null
        seeded = false
    }

    suspend fun clearDatabase() = withContext(dispatchers.io) {
        database?.finPilotDatabaseQueries?.transaction {
            finPilotDatabaseQueries.deleteAllExpenses()
            finPilotDatabaseQueries.deleteAllBudgets()
            finPilotDatabaseQueries.deleteAllInsights()
            finPilotDatabaseQueries.deleteAllSubscriptions()
            finPilotDatabaseQueries.deleteAllChatMessages()
            finPilotDatabaseQueries.deleteAllUserProfiles()
            finPilotDatabaseQueries.deleteAllSyncOutbox()
        }
    }
}
