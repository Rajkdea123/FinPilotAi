package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.SyncStatus
import com.example.myapplication.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow

class ObserveSyncStatusUseCase(
    private val syncRepository: SyncRepository
) {
    operator fun invoke(): Flow<SyncStatus> {
        return syncRepository.observeStatus()
    }
}
