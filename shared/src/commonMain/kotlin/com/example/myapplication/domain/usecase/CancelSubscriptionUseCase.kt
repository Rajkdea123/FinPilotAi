package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.InsightsRepository

class CancelSubscriptionUseCase(
    private val insightsRepository: InsightsRepository
) {
    // TODO: Real cancellation happens outside the app; this only updates the tracker
    suspend operator fun invoke(id: String) {
        insightsRepository.markCancelled(id)
    }
}
