package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.ConflictChoice
import com.example.myapplication.domain.repository.SyncRepository

class ResolveConflictUseCase(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(conflictId: String, choice: ConflictChoice) {
        syncRepository.resolveConflict(conflictId, choice)
    }
}
