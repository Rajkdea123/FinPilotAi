package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Insight
import com.example.myapplication.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow

class GetInsightsUseCase(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(): Flow<List<Insight>> {
        return insightsRepository.observeInsights()
    }
}
