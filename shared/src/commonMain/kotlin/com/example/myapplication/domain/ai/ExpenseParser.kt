package com.example.myapplication.domain.ai

import com.example.myapplication.domain.model.ParsedExpense

interface ExpenseParser {
    suspend fun parse(text: String): Result<ParsedExpense>
}
