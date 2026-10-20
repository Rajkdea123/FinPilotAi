package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Subscription
import com.example.myapplication.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow

class GetSubscriptionsUseCase(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(): Flow<List<Subscription>> {
        return insightsRepository.observeSubscriptions()
    }
}
