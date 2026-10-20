package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class GetAnalyticsUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(period: Period): Flow<AnalyticsSummary> {
        return expenseRepository.observeAnalytics(period)
    }
}
