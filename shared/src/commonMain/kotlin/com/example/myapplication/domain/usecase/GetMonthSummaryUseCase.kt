package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class GetMonthSummaryUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(): Flow<MonthSummary> {
        return expenseRepository.observeMonthSummary()
    }
}
