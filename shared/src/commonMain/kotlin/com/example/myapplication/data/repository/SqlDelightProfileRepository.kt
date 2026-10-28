package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.di.AppCoroutineDispatchers
import com.example.myapplication.domain.model.ProfileStats
import com.example.myapplication.domain.model.UserProfile
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

    override fun observeProfile(): Flow<UserProfile> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.userProfileQueries.selectUserProfile()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities ->
                    val profile = entities.firstOrNull()
                    if (profile != null) {
                        UserProfile(
                            id = profile.id,
                            name = profile.name,
                            email = profile.email,
                            avatarUrl = profile.avatarUrl
                        )
                    } else {
                        // Return empty profile if none exists
                        UserProfile(
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
                .map { expenses ->
                    ProfileStats(
                        expenseCount = expenses.size,
                        categoryCount = expenses.map { it.category }.distinct().size,
                        deviceCount = 1 // TODO: Track multiple devices
                    )
                }
                .collect { emit(it) }
        }
    }

    override suspend fun update(profile: UserProfile): Result<Unit> = withContext(dispatchers.io) {
        try {
            getQueries().transaction {
                val existing = getQueries().userProfileQueries.selectUserProfile().executeAsOneOrNull()
                
                if (existing != null) {
                    // Update existing profile
                    getQueries().userProfileQueries.updateUserProfile(
                        name = profile.name,
                        email = profile.email,
                        avatarUrl = profile.avatarUrl,
                        updatedAt = clock.now().toEpochMilliseconds(),
                        id = profile.id
                    )
                } else {
                    // Insert new profile
                    @OptIn(ExperimentalUuidApi::class)
                    val id = if (profile.id.isEmpty()) Uuid.random().toString() else profile.id
                    getQueries().userProfileQueries.insertUserProfile(
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
                addSyncOutboxEntry("UserProfile", profile.id, "UPDATE", profile)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addSyncOutboxEntry(tableName: String, recordId: String, operation: String, payload: UserProfile) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = """{"id":"${payload.id}","name":"${payload.name}","email":"${payload.email}","avatarUrl":"${payload.avatarUrl ?: ""}"}"""
        
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
}
