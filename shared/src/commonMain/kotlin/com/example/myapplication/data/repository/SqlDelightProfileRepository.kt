package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.db.Expense as DbExpense
import com.example.myapplication.db.UserProfile as DbUserProfile
import com.example.myapplication.di.core.AppCoroutineDispatchers
import com.example.myapplication.domain.model.ProfileStats
import com.example.myapplication.domain.model.UserProfile as DomainUserProfile
import com.example.myapplication.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightProfileRepository(
    private val databaseManager: DatabaseManager,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock
) : ProfileRepository {

    private suspend fun getQueries(): FinPilotDatabase = databaseManager.getDatabase()

    override fun observeProfile(): Flow<DomainUserProfile> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.userProfileQueries.selectUserProfile()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities: List<DbUserProfile> ->
                    val profile = entities.firstOrNull()
                    if (profile != null) {
                        DomainUserProfile(
                            id = profile.id,
                            name = profile.name,
                            email = profile.email,
                            avatarUrl = profile.avatarUrl
                        )
                    } else {
                        // Return empty profile if none exists
                        DomainUserProfile(
                            id = "",
                            name = "",
                            email = "",
                            avatarUrl = null
                        )
                    }
                }
                .collect { emit(it) }
        }
    }

    override fun observeStats(): Flow<ProfileStats> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.expenseQueries.selectAllExpenses()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { expenses: List<DbExpense> ->
                    ProfileStats(
                        expenseCount = expenses.size,
                        categoryCount = expenses.map { it.category }.distinct().size,
                        deviceCount = 1 // TODO: Track multiple devices
                    )
                }
                .collect { emit(it) }
        }
    }

    override suspend fun update(profile: DomainUserProfile): Result<Unit> = withContext(dispatchers.io) {
        try {
            val db = getQueries()
            db.transaction {
                val existing = db.userProfileQueries.selectUserProfile().executeAsOneOrNull()
                
                if (existing != null) {
                    // Update existing profile
                    db.userProfileQueries.updateUserProfile(
                        name = profile.name,
                        email = profile.email,
                        avatarUrl = profile.avatarUrl,
                        updatedAt = clock.now().toEpochMilliseconds(),
                        id = profile.id
                    )
                } else {
                    // Insert new profile
                    @OptIn(ExperimentalUuidApi::class)
                    val id = profile.id.ifEmpty { Uuid.random().toString() }
                    db.userProfileQueries.insertUserProfile(
                        id = id,
                        name = profile.name,
                        email = profile.email,
                        avatarUrl = profile.avatarUrl,
                        version = 1,
                        updatedAt = clock.now().toEpochMilliseconds(),
                        deviceId = "device-1",
                        isDeleted = 0,
                        syncState = "PENDING"
                    )
                }

                // Add sync outbox entry
                addSyncOutboxEntry(db, "UserProfile", profile.id, "UPDATE", profile)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addSyncOutboxEntry(db: FinPilotDatabase, tableName: String, recordId: String, operation: String, payload: DomainUserProfile) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = """{"id":"${payload.id}","name":"${payload.name}","email":"${payload.email}","avatarUrl":"${payload.avatarUrl ?: ""}"}"""
        
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
