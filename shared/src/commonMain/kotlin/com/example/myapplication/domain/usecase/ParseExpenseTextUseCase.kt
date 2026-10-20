package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.ai.ExpenseParser
import com.example.myapplication.domain.model.ParsedExpense

class ParseExpenseTextUseCase(
    private val expenseParser: ExpenseParser
) {
    suspend operator fun invoke(text: String): Result<ParsedExpense> {
        return expenseParser.parse(text)
    }
}
