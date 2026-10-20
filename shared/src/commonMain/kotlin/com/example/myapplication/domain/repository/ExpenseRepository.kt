package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.domain.model.Period
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeRecent(limit: Int): Flow<List<Expense>>
    fun observeMonthSummary(): Flow<MonthSummary>
    fun observeAnalytics(period: Period): Flow<AnalyticsSummary>
    suspend fun addExpense(expense: Expense)
}
