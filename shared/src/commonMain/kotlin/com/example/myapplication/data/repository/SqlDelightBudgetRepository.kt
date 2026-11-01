package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.db.Budget as DbBudget
import com.example.myapplication.di.core.AppCoroutineDispatchers
import com.example.myapplication.domain.model.Budget as DomainBudget
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightBudgetRepository(
    private val databaseManager: DatabaseManager,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock
) : BudgetRepository {

    private suspend fun getQueries(): FinPilotDatabase = databaseManager.getDatabase()

    override fun observeBudget(): Flow<DomainBudget> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.budgetQueries.selectAllBudgets()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities: List<DbBudget> ->
                    val total = entities.firstOrNull { it.category == "TOTAL" }?.total ?: 0L
                    val byCategory = entities.filter { it.category != "TOTAL" }.associate { entity ->
                        val cat = runCatching { Category.valueOf(entity.category) }.getOrDefault(Category.Food)
                        cat to entity.budgetLimit
                    }
                    DomainBudget(total, byCategory)
                }
                .collect { emit(it) }
        }
    }

    override suspend fun save(budget: DomainBudget): Result<Unit> = withContext(dispatchers.io) {
        try {
            val db = getQueries()
            db.transaction {
                // Delete existing budget entries
                db.budgetQueries.selectAllBudgets().executeAsList().forEach { existing ->
                    db.budgetQueries.deleteBudget(
                        updatedAt = clock.now().toEpochMilliseconds(),
                        id = existing.id
                    )
                }

                // Insert total budget entry
                @OptIn(ExperimentalUuidApi::class)
                val totalId = Uuid.random().toString()
                db.budgetQueries.insertBudget(
                    id = totalId,
                    total = budget.totalPaise,
                    category = "TOTAL",
                    budgetLimit = budget.totalPaise,
                    version = 1,
                    updatedAt = clock.now().toEpochMilliseconds(),
                    deviceId = "device-1",
                    isDeleted = 0,
                    syncState = "PENDING"
                )

                // Insert per-category budget entries
                budget.byCategory.forEach { (category, limit) ->
                    @OptIn(ExperimentalUuidApi::class)
                    val categoryId = Uuid.random().toString()
                    db.budgetQueries.insertBudget(
                        id = categoryId,
                        total = budget.totalPaise,
                        category = category.name,
                        budgetLimit = limit,
                        version = 1,
                        updatedAt = clock.now().toEpochMilliseconds(),
                        deviceId = "device-1",
                        isDeleted = 0,
                        syncState = "PENDING"
                    )
                }

                // Add sync outbox entries
                addSyncOutboxEntry(db, "Budget", totalId, "CREATE", budget)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun suggestTotal(): Long = withContext(dispatchers.io) {
        // For now, return a default suggestion
        // TODO: Calculate based on average monthly spending
        500000L // 5000 rupees in paise
    }

    private fun addSyncOutboxEntry(db: FinPilotDatabase, tableName: String, recordId: String, operation: String, payload: Any) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = when (payload) {
            is DomainBudget -> """{"total":${payload.totalPaise},"categories":${payload.byCategory.mapKeys { it.key.name }}}"""
            else -> """{"id":"$recordId"}"""
        }
        
        db.syncOutboxQueries.insertSyncOutbox(
            id = id,
            tableName = tableName,
            recordId = recordId,
            operation = operation,
            payload = payloadJson,
            createdAt = clock.now().toEpochMilliseconds(),
            isProcessed = 0,
            processedAt = null
        )
    }
}
