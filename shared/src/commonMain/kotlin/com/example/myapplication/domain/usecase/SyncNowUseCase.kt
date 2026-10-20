package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.SyncRepository

class SyncNowUseCase(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke() {
        syncRepository.syncNow()
    }
}
