package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.ConflictChoice
import com.example.myapplication.domain.model.ConflictVersion
import com.example.myapplication.domain.model.SyncConflict
import com.example.myapplication.domain.model.SyncMetadata
import com.example.myapplication.domain.model.SyncStatus
import com.example.myapplication.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class FakeSyncRepository : SyncRepository {

    private val _status = MutableStateFlow<SyncStatus>(
        SyncStatus.Offline(pendingCount = 3)
    )

    private val _conflicts = MutableStateFlow<List<SyncConflict>>(
        listOf(
            SyncConflict(
                id = "conflict1",
                expenseId = "expense1",
                local = ConflictVersion(
                    deviceLabel = "This phone",
                    amountPaise = 500_00L,
                    metadata = SyncMetadata(
                        id = "meta1",
                        version = 1L,
                        updatedAt = 1000L,
                        deviceId = "android123"
                    )
                ),
                remote = ConflictVersion(
                    deviceLabel = "iPhone",
                    amountPaise = 600_00L,
                    metadata = SyncMetadata(
                        id = "meta2",
                        version = 2L,
                        updatedAt = 2000L,
                        deviceId = "iphone456"
                    )
                )
            )
        )
    )

    private var isOnline = false

    // TODO: Replace with SQLDelight outbox and Ktor sync endpoint

    override fun observeStatus(): Flow<SyncStatus> {
        return _status
    }

    override fun observeConflicts(): Flow<List<SyncConflict>> {
        return _conflicts
    }

    override suspend fun syncNow() {
        if (!isOnline) {
            _status.value = SyncStatus.Error("No internet connection", pendingCount = 3)
            return
        }

        if (_conflicts.value.isNotEmpty()) {
            _status.value = SyncStatus.Error("Resolve conflicts first", pendingCount = 3)
            return
        }

        _status.value = SyncStatus.Syncing(pendingCount = 3)
        // Simulate sync delay
        kotlinx.coroutines.delay(2000)
        _status.value = SyncStatus.Synced(lastSyncedAt = Clock.System.now())
    }

    override suspend fun resolveConflict(conflictId: String, choice: ConflictChoice) {
        _conflicts.value = _conflicts.value.filterNot { it.id == conflictId }
        
        // If no more conflicts and online, sync
        if (_conflicts.value.isEmpty() && isOnline) {
            syncNow()
        }
    }

    fun setOnline(online: Boolean) {
        isOnline = online
        if (online && _status.value is SyncStatus.Offline) {
            // Will sync when auto-sync is triggered
        }
    }
}
