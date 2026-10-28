package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.DateRange
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.model.SortOrder
import com.example.myapplication.domain.model.TransactionFilter
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeRecent(limit: Int): Flow<List<Expense>>
    fun observeMonthSummary(): Flow<MonthSummary>
    fun observeAnalytics(period: Period): Flow<AnalyticsSummary>
    fun observeAll(filter: TransactionFilter, sort: SortOrder): Flow<List<Expense>>
    suspend fun getExpenseById(id: String): Expense?
    suspend fun addExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun delete(id: String)
    suspend fun restore(expense: Expense)
}
