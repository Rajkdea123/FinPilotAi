package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.di.AppCoroutineDispatchers
import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.DateRange
import com.example.myapplication.domain.model.Expense
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
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightExpenseRepository(
    private val databaseManager: DatabaseManager,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock
) : ExpenseRepository {

    private suspend fun getQueries(): FinPilotDatabase = databaseManager.getDatabase()

    override fun observeRecent(limit: Int): Flow<List<Expense>> {
        // For now, use observeAll with a date filter for recent
        return observeAll(TransactionFilter(), SortOrder.DATE_DESC)
            .map { it.take(limit) }
    }

    override fun observeMonthSummary(): Flow<MonthSummary> {
        val now = clock.now()
        val monthKey = "${now.toLocalDateTime(TimeZone.currentSystemDefault()).year}-${now.toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber}"
        
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.expenseQueries.selectMonthSummary(monthKey)
                .asFlow()
                .mapToList(dispatchers.io)
                .map { queryResult ->
                    val total = queryResult.firstOrNull()?.totalSpent ?: 0L
                    val count = queryResult.firstOrNull()?.transactionCount ?: 0L
                    MonthSummary(total, count.toInt())
                }
                .collect { emit(it) }
        }
    }

    override fun observeAnalytics(period: Period): Flow<AnalyticsSummary> {
        val now = clock.now()
        val dateRange = when (period) {
            Period.WEEK -> DateRange(
                start = now.minus(kotlin.datetime.DateTimePeriod(days = 7)),
                end = now
            )
            Period.MONTH -> DateRange(
                start = now.minus(kotlin.datetime.DateTimePeriod(months = 1)),
                end = now
            )
            Period.YEAR -> DateRange(
                start = now.minus(kotlin.datetime.DateTimePeriod(years = 1)),
                end = now
            )
        }

        return observeAll(TransactionFilter(dateRange = dateRange), SortOrder.DATE_DESC)
            .map { expenses ->
                val totalSpent = expenses.sumOf { it.amountPaise }
                val categoryBreakdown = expenses.groupBy { it.category }
                    .mapValues { it.value.sumOf { e -> e.amountPaise } }
                AnalyticsSummary(totalSpent, categoryBreakdown)
            }
    }

    override fun observeAll(filter: TransactionFilter, sort: SortOrder): Flow<List<Expense>> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            val query = when {
                filter.query != null -> {
                    queries.expenseQueries.searchExpenses("%${filter.query}%")
                }
                filter.category != null -> {
                    queries.expenseQueries.selectExpensesByCategory(filter.category.name)
                }
                filter.paymentMethod != null -> {
                    queries.expenseQueries.selectExpensesByPaymentMethod(filter.paymentMethod)
                }
                filter.dateRange != null -> {
                    queries.expenseQueries.selectExpensesByDateRange(
                        filter.dateRange.start.toEpochMilliseconds(),
                        filter.dateRange.end.toEpochMilliseconds()
                    )
                }
                else -> {
                    queries.expenseQueries.selectAllExpenses()
                }
            }

            query.asFlow()
                .mapToList(dispatchers.io)
                .map { entities ->
                    entities.map { it.toDomainModel() }
                        .let { expenses ->
                            when (sort) {
                                SortOrder.DATE_ASC -> expenses.sortedBy { it.timestamp }
                                SortOrder.DATE_DESC -> expenses.sortedByDescending { it.timestamp }
                                SortOrder.AMOUNT_ASC -> expenses.sortedBy { it.amountPaise }
                                SortOrder.AMOUNT_DESC -> expenses.sortedByDescending { it.amountPaise }
                            }
                        }
                }
                .collect { emit(it) }
        }
    }

    override suspend fun getExpenseById(id: String): Expense? = withContext(dispatchers.io) {
        getQueries().expenseQueries.selectExpenseById(id).executeAsOneOrNull()?.toDomainModel()
    }

    override suspend fun addExpense(expense: Expense) = withContext(dispatchers.io) {
        getQueries().transaction {
            getQueries().expenseQueries.insertExpense(
                id = expense.id,
                amount = expense.amountPaise,
                category = expense.category.name,
                paymentMethod = expense.paymentMethod,
                date = expense.timestamp.toEpochMilliseconds(),
                description = expense.merchant,
                version = 1,
                updatedAt = clock.now().toEpochMilliseconds(),
                deviceId = "device-1", // TODO: Get from platform
                isDeleted = 0,
                syncState = "PENDING"
            )

            // Add sync outbox entry
            addSyncOutboxEntry("Expense", expense.id, "CREATE", expense)
        }
    }

    override suspend fun updateExpense(expense: Expense) = withContext(dispatchers.io) {
        getQueries().transaction {
            getQueries().expenseQueries.updateExpense(
                amount = expense.amountPaise,
                category = expense.category.name,
                paymentMethod = expense.paymentMethod,
                date = expense.timestamp.toEpochMilliseconds(),
                description = expense.merchant,
                updatedAt = clock.now().toEpochMilliseconds(),
                id = expense.id
            )

            // Add sync outbox entry
            addSyncOutboxEntry("Expense", expense.id, "UPDATE", expense)
        }
    }

    override suspend fun delete(id: String) = withContext(dispatchers.io) {
        getQueries().transaction {
            getQueries().expenseQueries.deleteExpense(
                updatedAt = clock.now().toEpochMilliseconds(),
                id = id
            )

            // Add sync outbox entry
            addSyncOutboxEntry("Expense", id, "DELETE", mapOf("id" to id))
        }
    }

    override suspend fun restore(expense: Expense) = withContext(dispatchers.io) {
        getQueries().transaction {
            // Restore by updating isDeleted to 0
            getQueries().expenseQueries.updateExpense(
                amount = expense.amountPaise,
                category = expense.category.name,
                paymentMethod = expense.paymentMethod,
                date = expense.timestamp.toEpochMilliseconds(),
                description = expense.merchant,
                updatedAt = clock.now().toEpochMilliseconds(),
                id = expense.id
            )

            // Add sync outbox entry
            addSyncOutboxEntry("Expense", expense.id, "UPDATE", expense)
        }
    }

    private fun addSyncOutboxEntry(tableName: String, recordId: String, operation: String, payload: Any) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = when (payload) {
            is Expense -> """{"id":"${payload.id}","amount":${payload.amountPaise},"category":"${payload.category.name}","paymentMethod":"${payload.paymentMethod}","date":${payload.timestamp.toEpochMilliseconds()},"description":"${payload.merchant}"}"""
            is Map<*, *> -> payload.toString()
            else -> """{"id":"$recordId"}"""
        }
        
        getQueries().syncOutboxQueries.insertSyncOutbox(
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

    private fun com.example.myapplication.db.Expense.toDomainModel(): Expense {
        return Expense(
            id = id,
            amountPaise = amount,
            category = Category.valueOf(category),
            merchant = description,
            paymentMethod = paymentMethod,
            timestamp = Instant.fromEpochMilliseconds(date)
        )
    }
}
