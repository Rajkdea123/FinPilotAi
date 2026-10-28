package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.repository.ExpenseRepository

class RestoreExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense) {
        expenseRepository.restore(expense)
    }
}
