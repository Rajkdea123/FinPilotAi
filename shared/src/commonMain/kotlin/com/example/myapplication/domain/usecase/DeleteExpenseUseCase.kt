package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.repository.ExpenseRepository

class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(id: String) {
        expenseRepository.delete(id)
    }
}
