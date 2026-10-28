package com.example.myapplication.network.api

import com.example.myapplication.network.dto.BudgetDto
import com.example.myapplication.network.dto.CreateBudgetRequest
import com.example.myapplication.network.dto.UpdateBudgetRequest

interface BudgetApi {
    suspend fun getBudgets(): List<BudgetDto>
    suspend fun createBudget(request: CreateBudgetRequest): BudgetDto
    suspend fun updateBudget(request: UpdateBudgetRequest): BudgetDto
}
