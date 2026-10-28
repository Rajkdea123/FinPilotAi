package com.example.myapplication.sync

import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.di.AppCoroutineDispatchers
import com.example.myapplication.network.api.BudgetApi
import com.example.myapplication.network.api.ChatApi
import com.example.myapplication.network.api.ExpenseApi
import com.example.myapplication.network.api.SubscriptionApi
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class SyncEngine(
    private val databaseManager: DatabaseManager,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock,
    private val expenseApi: ExpenseApi,
    private val budgetApi: BudgetApi,
    private val subscriptionApi: SubscriptionApi,
    private val chatApi: ChatApi
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    enum class ConflictResolution {
        SERVER_WINS,
        CLIENT_WINS,
        LAST_WRITE_WINS
    }
    
    private val conflictResolution = ConflictResolution.SERVER_WINS
    
    suspend fun syncPendingChanges(): SyncResult = withContext(dispatchers.io) {
        val database = databaseManager.getDatabase()
        var successCount = 0
        var failureCount = 0
        
        try {
            val pendingEntries = database.syncOutboxQueries.selectPendingSyncOutbox().executeAsList()
            
            for (entry in pendingEntries) {
                try {
                    when (entry.tableName) {
                        "Expense" -> syncExpense(entry, database)
                        "Budget" -> syncBudget(entry, database)
                        "Subscription" -> syncSubscription(entry, database)
                        "ChatMessage" -> syncChatMessage(entry, database)
                        else -> {
                            // Unknown table, mark as processed
                            markAsProcessed(entry.id, database)
                        }
                    }
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    // Log error but continue with other entries
                }
            }
            
            SyncResult(successCount, failureCount)
        } catch (e: Exception) {
            SyncResult(0, 1)
        }
    }
    
    private suspend fun syncExpense(entry: com.example.myapplication.db.SyncOutbox, database: com.example.myapplication.db.FinPilotDatabase) {
        when (entry.operation) {
            "CREATE" -> {
                // Parse payload and call API
                expenseApi.createExpense(parseExpenseCreateRequest(entry.payload))
                markAsProcessed(entry.id, database)
            }
            "UPDATE" -> {
                val request = parseExpenseUpdateRequest(entry.payload)
                val localExpense = database.expenseQueries.selectExpenseById(request.id).executeAsOneOrNull()
                
                if (localExpense != null && localExpense.syncState == "CONFLICT") {
                    // Resolve conflict based on strategy
                    when (conflictResolution) {
                        ConflictResolution.SERVER_WINS -> {
                            // Keep server version, update local to match
                            val serverExpense = expenseApi.getExpenseById(request.id)
                            updateLocalExpenseFromServer(serverExpense, database)
                            markAsProcessed(entry.id, database)
                        }
                        ConflictResolution.CLIENT_WINS -> {
                            // Force client version to server
                            expenseApi.updateExpense(request)
                            database.expenseQueries.updateExpenseSyncState("SYNCED", request.id)
                            markAsProcessed(entry.id, database)
                        }
                        ConflictResolution.LAST_WRITE_WINS -> {
                            // Compare timestamps and use latest
                            if (localExpense.updatedAt > request.version.toLong()) {
                                // Client is newer, push to server
                                expenseApi.updateExpense(request)
                            } else {
                                // Server is newer, pull from server
                                val serverExpense = expenseApi.getExpenseById(request.id)
                                updateLocalExpenseFromServer(serverExpense, database)
                            }
                            markAsProcessed(entry.id, database)
                        }
                    }
                } else {
                    expenseApi.updateExpense(request)
                    markAsProcessed(entry.id, database)
                }
            }
            "DELETE" -> {
                expenseApi.deleteExpense(parseExpenseDeleteRequest(entry.payload))
                markAsProcessed(entry.id, database)
            }
            else -> markAsProcessed(entry.id, database)
        }
    }
    
    private fun updateLocalExpenseFromServer(serverExpense: com.example.myapplication.network.dto.ExpenseDto, database: com.example.myapplication.db.FinPilotDatabase) {
        database.expenseQueries.updateExpense(
            amount = serverExpense.amountPaise,
            category = serverExpense.category,
            paymentMethod = serverExpense.paymentMethod,
            date = serverExpense.timestamp,
            description = serverExpense.merchant,
            updatedAt = serverExpense.updatedAt,
            id = serverExpense.id
        )
        database.expenseQueries.updateExpenseSyncState("SYNCED", serverExpense.id)
    }
    
    private suspend fun syncBudget(entry: com.example.myapplication.db.SyncOutbox, database: com.example.myapplication.db.FinPilotDatabase) {
        when (entry.operation) {
            "CREATE" -> {
                budgetApi.createBudget(parseBudgetCreateRequest(entry.payload))
                markAsProcessed(entry.id, database)
            }
            "UPDATE" -> {
                budgetApi.updateBudget(parseBudgetUpdateRequest(entry.payload))
                markAsProcessed(entry.id, database)
            }
            else -> markAsProcessed(entry.id, database)
        }
    }
    
    private suspend fun syncSubscription(entry: com.example.myapplication.db.SyncOutbox, database: com.example.myapplication.db.FinPilotDatabase) {
        when (entry.operation) {
            "CREATE" -> {
                subscriptionApi.createSubscription(parseSubscriptionCreateRequest(entry.payload))
                markAsProcessed(entry.id, database)
            }
            "UPDATE" -> {
                subscriptionApi.updateSubscription(parseSubscriptionUpdateRequest(entry.payload))
                markAsProcessed(entry.id, database)
            }
            else -> markAsProcessed(entry.id, database)
        }
    }
    
    private suspend fun syncChatMessage(entry: com.example.myapplication.db.SyncOutbox, database: com.example.myapplication.db.FinPilotDatabase) {
        when (entry.operation) {
            "CREATE" -> {
                chatApi.createChatMessage(parseChatCreateRequest(entry.payload))
                markAsProcessed(entry.id, database)
            }
            else -> markAsProcessed(entry.id, database)
        }
    }
    
    private fun markAsProcessed(entryId: String, database: com.example.myapplication.db.FinPilotDatabase) {
        database.syncOutboxQueries.markSyncOutboxProcessed(
            processedAt = clock.now().toEpochMilliseconds(),
            id = entryId
        )
    }
    
    // Parse functions - these would parse the JSON payload into request objects
    private fun parseExpenseCreateRequest(payload: String): com.example.myapplication.network.dto.CreateExpenseRequest {
        return json.decodeFromString(payload)
    }
    
    private fun parseExpenseUpdateRequest(payload: String): com.example.myapplication.network.dto.UpdateExpenseRequest {
        return json.decodeFromString(payload)
    }
    
    private fun parseExpenseDeleteRequest(payload: String): com.example.myapplication.network.dto.DeleteExpenseRequest {
        return json.decodeFromString(payload)
    }
    
    private fun parseBudgetCreateRequest(payload: String): com.example.myapplication.network.dto.CreateBudgetRequest {
        return json.decodeFromString(payload)
    }
    
    private fun parseBudgetUpdateRequest(payload: String): com.example.myapplication.network.dto.UpdateBudgetRequest {
        return json.decodeFromString(payload)
    }
    
    private fun parseSubscriptionCreateRequest(payload: String): com.example.myapplication.network.dto.CreateSubscriptionRequest {
        return json.decodeFromString(payload)
    }
    
    private fun parseSubscriptionUpdateRequest(payload: String): com.example.myapplication.network.dto.UpdateSubscriptionRequest {
        return json.decodeFromString(payload)
    }
    
    private fun parseChatCreateRequest(payload: String): com.example.myapplication.network.dto.CreateChatMessageRequest {
        return json.decodeFromString(payload)
    }
}

data class SyncResult(
    val successCount: Int,
    val failureCount: Int
)
