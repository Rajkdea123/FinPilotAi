package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.db.Expense as DbExpense
import com.example.myapplication.di.core.AppCoroutineDispatchers
import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.CategorySpend
import com.example.myapplication.domain.model.Expense as DomainExpense
import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.model.SortOrder
import com.example.myapplication.domain.model.TransactionFilter
import com.example.myapplication.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightExpenseRepository(
    private val databaseManager: DatabaseManager,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock
) : ExpenseRepository {

    private suspend fun getQueries(): FinPilotDatabase = databaseManager.getDatabase()

    override fun observeRecent(limit: Int): Flow<List<DomainExpense>> {
        return observeAll(TransactionFilter(), SortOrder.NewestFirst)
            .map { it.take(limit) }
    }

    override fun observeMonthSummary(): Flow<MonthSummary> {
        val now = clock.now()
        val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val monthKey = "${localDate.year}-${localDate.monthNumber.toString().padStart(2, '0')}"
        
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.expenseQueries.selectMonthSummary(monthKey)
                .asFlow()
                .mapToList(dispatchers.io)
                .map { queryResult ->
                    val total = queryResult.firstOrNull()?.totalSpent ?: 0L
                    MonthSummary(
                        totalPaise = total,
                        previousMonthTotalPaise = 0L,
                        budgetPaise = 500000L
                    )
                }
                .collect { emit(it) }
        }
    }

    override fun observeAnalytics(period: Period): Flow<AnalyticsSummary> {
        return observeAll(TransactionFilter(), SortOrder.NewestFirst)
            .map { expenses ->
                val totalSpent = expenses.sumOf { it.amountPaise }
                val categories = expenses.groupBy { it.category }
                    .map { (cat, list) ->
                        val amount = list.sumOf { it.amountPaise }
                        val ratio = if (totalSpent > 0) amount.toFloat() / totalSpent.toFloat() else 0f
                        CategorySpend(cat, amount, ratio)
                    }
                AnalyticsSummary(
                    period = period,
                    totalPaise = totalSpent,
                    points = emptyList(),
                    highlightIndex = 0,
                    categories = categories
                )
            }
    }

    override fun observeAll(filter: TransactionFilter, sort: SortOrder): Flow<List<DomainExpense>> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            val query = when {
                filter.query.isNotEmpty() -> {
                    queries.expenseQueries.searchExpenses("%${filter.query}%")
                }
                filter.categories.isNotEmpty() -> {
                    queries.expenseQueries.selectExpensesByCategory(filter.categories.first().name)
                }
                filter.paymentMethods.isNotEmpty() -> {
                    queries.expenseQueries.selectExpensesByPaymentMethod(filter.paymentMethods.first())
                }
                filter.dateRange != null -> {
                    val startMs = filter.dateRange.start.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    val endMs = filter.dateRange.end.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    queries.expenseQueries.selectExpensesByDateRange(startMs, endMs)
                }
                else -> {
                    queries.expenseQueries.selectAllExpenses()
                }
            }

            query.asFlow()
                .mapToList(dispatchers.io)
                .map { entities: List<DbExpense> ->
                    entities.map { it.toDomainModel() }
                        .let { expenses ->
                            when (sort) {
                                SortOrder.OldestFirst -> expenses.sortedBy { it.timestamp }
                                SortOrder.NewestFirst -> expenses.sortedByDescending { it.timestamp }
                                SortOrder.HighestAmount -> expenses.sortedByDescending { it.amountPaise }
                            }
                        }
                }
                .collect { emit(it) }
        }
    }

    override suspend fun getExpenseById(id: String): DomainExpense? = withContext(dispatchers.io) {
        getQueries().expenseQueries.selectExpenseById(id).executeAsOneOrNull()?.toDomainModel()
    }

    override suspend fun addExpense(expense: DomainExpense) = withContext(dispatchers.io) {
        val db = getQueries()
        db.transaction {
            db.expenseQueries.insertExpense(
                id = expense.id,
                amount = expense.amountPaise,
                category = expense.category.name,
                paymentMethod = expense.paymentMethod,
                date = expense.timestamp.toEpochMilliseconds(),
                description = expense.merchant,
                version = 1,
                updatedAt = clock.now().toEpochMilliseconds(),
                deviceId = "device-1",
                isDeleted = 0,
                syncState = "PENDING"
            )

            addSyncOutboxEntry(db, "Expense", expense.id, "CREATE", expense)
        }
    }

    override suspend fun updateExpense(expense: DomainExpense) = withContext(dispatchers.io) {
        val db = getQueries()
        db.transaction {
            db.expenseQueries.updateExpense(
                amount = expense.amountPaise,
                category = expense.category.name,
                paymentMethod = expense.paymentMethod,
                date = expense.timestamp.toEpochMilliseconds(),
                description = expense.merchant,
                updatedAt = clock.now().toEpochMilliseconds(),
                id = expense.id
            )

            addSyncOutboxEntry(db, "Expense", expense.id, "UPDATE", expense)
        }
    }

    override suspend fun delete(id: String) = withContext(dispatchers.io) {
        val db = getQueries()
        db.transaction {
            db.expenseQueries.deleteExpense(
                updatedAt = clock.now().toEpochMilliseconds(),
                id = id
            )

            addSyncOutboxEntry(db, "Expense", id, "DELETE", mapOf("id" to id))
        }
    }

    override suspend fun restore(expense: DomainExpense) = withContext(dispatchers.io) {
        val db = getQueries()
        db.transaction {
            db.expenseQueries.updateExpense(
                amount = expense.amountPaise,
                category = expense.category.name,
                paymentMethod = expense.paymentMethod,
                date = expense.timestamp.toEpochMilliseconds(),
                description = expense.merchant,
                updatedAt = clock.now().toEpochMilliseconds(),
                id = expense.id
            )

            addSyncOutboxEntry(db, "Expense", expense.id, "UPDATE", expense)
        }
    }

    private fun addSyncOutboxEntry(db: FinPilotDatabase, tableName: String, recordId: String, operation: String, payload: Any) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = when (payload) {
            is DomainExpense -> """{"id":"${payload.id}","amount":${payload.amountPaise},"category":"${payload.category.name}","paymentMethod":"${payload.paymentMethod}","date":${payload.timestamp.toEpochMilliseconds()},"description":"${payload.merchant}"}"""
            is Map<*, *> -> payload.toString()
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

    private fun DbExpense.toDomainModel(): DomainExpense {
        val cat = runCatching { Category.valueOf(category) }.getOrDefault(Category.Food)
        return DomainExpense(
            id = id,
            amountPaise = amount,
            category = cat,
            merchant = description,
            paymentMethod = paymentMethod,
            timestamp = Instant.fromEpochMilliseconds(date)
        )
    }
}
