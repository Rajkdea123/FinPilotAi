package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.di.AppCoroutineDispatchers
import com.example.myapplication.domain.model.Insight
import com.example.myapplication.domain.model.InsightType
import com.example.myapplication.domain.model.Subscription
import com.example.myapplication.domain.model.SubscriptionStatus
import com.example.myapplication.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightInsightsRepository(
    private val databaseManager: DatabaseManager,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock
) : InsightsRepository {

    private suspend fun getQueries(): FinPilotDatabase = databaseManager.getDatabase()

    override fun observeInsights(): Flow<List<Insight>> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.insightQueries.selectAllInsights()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities ->
                    entities.map { it.toDomainModel() }
                }
                .collect { emit(it) }
        }
    }

    override suspend fun dismiss(id: String) = withContext(dispatchers.io) {
        getQueries().transaction {
            getQueries().insightQueries.deleteInsight(
                updatedAt = clock.now().toEpochMilliseconds(),
                id = id
            )

            // Add sync outbox entry
            addSyncOutboxEntry("Insight", id, "DELETE", mapOf("id" to id))
        }
    }

    override suspend fun restore(id: String) = withContext(dispatchers.io) {
        // For insights, restore would mean re-creating it
        // This is a placeholder - actual restore logic depends on business requirements
        getQueries().transaction {
            // Mark as not deleted by updating with sync state
            // Since we don't have the original insight data, this is a simplified version
            addSyncOutboxEntry("Insight", id, "UPDATE", mapOf("id" to id, "isDeleted" to 0))
        }
    }

    override fun observeSubscriptions(): Flow<List<Subscription>> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.subscriptionQueries.selectActiveSubscriptions()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities ->
                    entities.map { it.toDomainModel() }
                }
                .collect { emit(it) }
        }
    }

    override suspend fun markCancelled(id: String) = withContext(dispatchers.io) {
        getQueries().transaction {
            getQueries().subscriptionQueries.updateSubscription(
                name = "", // Will keep existing
                amount = 0, // Will keep existing
                category = "", // Will keep existing
                billingCycle = "", // Will keep existing
                nextBillingDate = 0, // Will keep existing
                isActive = 0,
                updatedAt = clock.now().toEpochMilliseconds(),
                id = id
            )

            // Add sync outbox entry
            addSyncOutboxEntry("Subscription", id, "UPDATE", mapOf("id" to id, "isActive" to 0))
        }
    }

    private fun addSyncOutboxEntry(tableName: String, recordId: String, operation: String, payload: Map<String, Any>) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = payload.entries.joinToString(",", "{", "}") { (key, value) -> "\"$key\":$value" }
        
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

    private fun com.example.myapplication.db.Insight.toDomainModel(): Insight {
        return Insight(
            id = id,
            type = when (category) {
                "UnusedSubscription" -> InsightType.UnusedSubscription
                "SpendingAlert" -> InsightType.SpendingAlert
                else -> InsightType.SpendingAlert
            },
            title = title,
            message = content,
            createdAt = Instant.fromEpochMilliseconds(date)
        )
    }

    private fun com.example.myapplication.db.Subscription.toDomainModel(): Subscription {
        val now = clock.now()
        val nextBilling = Instant.fromEpochMilliseconds(nextBillingDate)
        val renewsInDays = ((nextBilling - now).inWholeDays).toInt()
        
        return Subscription(
            id = id,
            name = name,
            amountPaise = amount,
            renewsInDays = renewsInDays,
            lastUsedDaysAgo = null, // TODO: Track last used date
            status = if (isActive == 1L) SubscriptionStatus.Active else SubscriptionStatus.Cancelled
        )
    }
}
