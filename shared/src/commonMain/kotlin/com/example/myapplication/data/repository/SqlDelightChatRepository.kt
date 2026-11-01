package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.db.ChatMessage as DbChatMessage
import com.example.myapplication.di.core.AppCoroutineDispatchers
import com.example.myapplication.domain.model.ChatMessage as DomainChatMessage
import com.example.myapplication.domain.model.MessageRole
import com.example.myapplication.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightChatRepository(
    private val databaseManager: DatabaseManager,
    private val dispatchers: AppCoroutineDispatchers,
    private val clock: Clock
) : ChatRepository {

    private suspend fun getQueries(): FinPilotDatabase = databaseManager.getDatabase()

    override fun observeMessages(): Flow<List<DomainChatMessage>> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.chatMessageQueries.selectAllChatMessages()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities: List<DbChatMessage> ->
                    entities.map { it.toDomainModel() }
                }
                .collect { emit(it) }
        }
    }

    override suspend fun save(message: DomainChatMessage) = withContext(dispatchers.io) {
        val db = getQueries()
        db.transaction {
            @OptIn(ExperimentalUuidApi::class)
            val id = message.id.ifEmpty { Uuid.random().toString() }
            
            db.chatMessageQueries.insertChatMessage(
                id = id,
                role = message.role.name,
                content = message.text,
                timestamp = message.createdAt.toEpochMilliseconds(),
                version = 1,
                updatedAt = clock.now().toEpochMilliseconds(),
                deviceId = "device-1",
                isDeleted = 0,
                syncState = "PENDING"
            )

            // Add sync outbox entry
            addSyncOutboxEntry(db, "ChatMessage", id, "CREATE", message)
        }
    }

    private fun addSyncOutboxEntry(db: FinPilotDatabase, tableName: String, recordId: String, operation: String, payload: DomainChatMessage) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = """{"id":"${payload.id}","role":"${payload.role.name}","content":"${payload.text}","timestamp":${payload.createdAt.toEpochMilliseconds()}}"""
        
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

    private fun DbChatMessage.toDomainModel(): DomainChatMessage {
        return DomainChatMessage(
            id = id,
            role = when (role) {
                "USER" -> MessageRole.User
                "ASSISTANT" -> MessageRole.Assistant
                else -> MessageRole.User
            },
            text = content,
            createdAt = Instant.fromEpochMilliseconds(timestamp)
        )
    }
}
