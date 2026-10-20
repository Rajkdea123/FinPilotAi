package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.repository.ExpenseRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class SaveExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(
        amountPaise: Long,
        category: Category,
        paymentMethod: String,
        date: kotlinx.datetime.LocalDate,
        description: String?
    ) {
        val expense = Expense(
            id = generateId(),
            amountPaise = amountPaise,
            category = category,
            merchant = description ?: "Unknown",
            paymentMethod = paymentMethod,
            timestamp = date.atTime(12, 0).toInstant(TimeZone.currentSystemDefault())
        )
        expenseRepository.addExpense(expense)
    }

    private fun generateId(): String {
        return "expense_${Clock.System.now().toEpochMilliseconds()}"
    }
}
