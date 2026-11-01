package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.db.Insight as DbInsight
import com.example.myapplication.db.Subscription as DbSubscription
import com.example.myapplication.di.core.AppCoroutineDispatchers
import com.example.myapplication.domain.model.Insight as DomainInsight
import com.example.myapplication.domain.model.InsightType
import com.example.myapplication.domain.model.Subscription as DomainSubscription
import com.example.myapplication.domain.model.SubscriptionStatus
import com.example.myapplication.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override fun observeInsights(): Flow<List<DomainInsight>> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.insightQueries.selectAllInsights()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities: List<DbInsight> ->
                    entities.map { it.toDomainModel() }
                }
                .collect { emit(it) }
        }
    }

    override suspend fun dismiss(id: String) = withContext(dispatchers.io) {
        val db = getQueries()
        db.transaction {
            db.insightQueries.deleteInsight(
                updatedAt = clock.now().toEpochMilliseconds(),
                id = id
            )

            // Add sync outbox entry
            addSyncOutboxEntry(db, "Insight", id, "DELETE", mapOf("id" to id))
        }
    }

    override suspend fun restore(id: String) = withContext(dispatchers.io) {
        val db = getQueries()
        db.transaction {
            addSyncOutboxEntry(db, "Insight", id, "UPDATE", mapOf("id" to id, "isDeleted" to 0))
        }
    }

    override fun observeSubscriptions(): Flow<List<DomainSubscription>> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.subscriptionQueries.selectActiveSubscriptions()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities: List<DbSubscription> ->
                    entities.map { it.toDomainModel() }
                }
                .collect { emit(it) }
        }
    }

    override suspend fun markCancelled(id: String) = withContext(dispatchers.io) {
        val db = getQueries()
        db.transaction {
            db.subscriptionQueries.updateSubscription(
                name = "",
                amount = 0,
                category = "",
                billingCycle = "",
                nextBillingDate = 0,
                isActive = 0,
                updatedAt = clock.now().toEpochMilliseconds(),
                id = id
            )

            // Add sync outbox entry
            addSyncOutboxEntry(db, "Subscription", id, "UPDATE", mapOf("id" to id, "isActive" to 0))
        }
    }

    private fun addSyncOutboxEntry(db: FinPilotDatabase, tableName: String, recordId: String, operation: String, payload: Map<String, Any>) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = payload.entries.joinToString(",", "{", "}") { (key, value) -> "\"$key\":$value" }
        
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

    private fun DbInsight.toDomainModel(): DomainInsight {
        return DomainInsight(
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

    private fun DbSubscription.toDomainModel(): DomainSubscription {
        val now = clock.now()
        val nextBilling = Instant.fromEpochMilliseconds(nextBillingDate)
        val renewsInDays = ((nextBilling - now).inWholeDays).toInt()
        
        return DomainSubscription(
            id = id,
            name = name,
            amountPaise = amount,
            renewsInDays = renewsInDays,
            lastUsedDaysAgo = null, // TODO: Track last used date
            status = if (isActive == 1L) SubscriptionStatus.Active else SubscriptionStatus.Cancelled
        )
    }
}
