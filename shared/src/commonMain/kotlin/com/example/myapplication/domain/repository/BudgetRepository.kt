package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudget(): Flow<Budget>
    suspend fun save(budget: Budget): Result<Unit>
    suspend fun suggestTotal(): Long
}
