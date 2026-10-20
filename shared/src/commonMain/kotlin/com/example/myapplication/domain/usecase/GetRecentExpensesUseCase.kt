package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

class GetRecentExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<Expense>> {
        return expenseRepository.observeRecent(limit)
    }
}
