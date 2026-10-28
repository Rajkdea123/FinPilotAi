package com.example.myapplication.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.myapplication.db.DatabaseManager
import com.example.myapplication.db.FinPilotDatabase
import com.example.myapplication.di.AppCoroutineDispatchers
import com.example.myapplication.domain.model.ChatMessage
import com.example.myapplication.domain.model.MessageRole
import com.example.myapplication.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
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

    override fun observeMessages(): Flow<List<ChatMessage>> {
        return kotlinx.coroutines.flow.flow {
            val queries = getQueries()
            queries.chatMessageQueries.selectAllChatMessages()
                .asFlow()
                .mapToList(dispatchers.io)
                .map { entities ->
                    entities.map { it.toDomainModel() }
                }
                .collect { emit(it) }
        }
    }

    override suspend fun save(message: ChatMessage) = withContext(dispatchers.io) {
        getQueries().transaction {
            @OptIn(ExperimentalUuidApi::class)
            val id = if (message.id.isEmpty()) Uuid.random().toString() else message.id
            
            getQueries().chatMessageQueries.insertChatMessage(
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
            addSyncOutboxEntry("ChatMessage", id, "CREATE", message)
        }
    }

    private fun addSyncOutboxEntry(tableName: String, recordId: String, operation: String, payload: ChatMessage) {
        @OptIn(ExperimentalUuidApi::class)
        val id = Uuid.random().toString()
        val payloadJson = """{"id":"${payload.id}","role":"${payload.role.name}","content":"${payload.text}","timestamp":${payload.createdAt.toEpochMilliseconds()}}"""
        
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

    private fun com.example.myapplication.db.ChatMessage.toDomainModel(): ChatMessage {
        return ChatMessage(
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
