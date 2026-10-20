package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.InsightsRepository

class RestoreInsightUseCase(
    private val insightsRepository: InsightsRepository
) {
    suspend operator fun invoke(id: String) {
        insightsRepository.restore(id)
    }
}
