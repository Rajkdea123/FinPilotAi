package com.example.myapplication.network.api

import com.example.myapplication.network.dto.CreateExpenseRequest
import com.example.myapplication.network.dto.DeleteExpenseRequest
import com.example.myapplication.network.dto.ExpenseDto
import com.example.myapplication.network.dto.UpdateExpenseRequest

interface ExpenseApi {
    suspend fun getExpenses(): List<ExpenseDto>
    suspend fun getExpenseById(id: String): ExpenseDto
    suspend fun createExpense(request: CreateExpenseRequest): ExpenseDto
    suspend fun updateExpense(request: UpdateExpenseRequest): ExpenseDto
    suspend fun deleteExpense(request: DeleteExpenseRequest)
}
