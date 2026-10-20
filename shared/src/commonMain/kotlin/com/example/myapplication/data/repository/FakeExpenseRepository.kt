package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class FakeExpenseRepository : ExpenseRepository {
    private val _expenses = MutableStateFlow(
        listOf(
            Expense(
                id = "1",
                amountPaise = 45000,
                category = Category.Food,
                merchant = "Zomato",
                paymentMethod = "UPI",
                timestamp = Clock.System.now()
            ),
            Expense(
                id = "2",
                amountPaise = 30000,
                category = Category.Transport,
                merchant = "Metro",
                paymentMethod = "Card",
                timestamp = Clock.System.now()
            ),
            Expense(
                id = "3",
                amountPaise = 200000,
                category = Category.Bills,
                merchant = "Rent",
                paymentMethod = "Bank Transfer",
                timestamp = Clock.System.now()
            )
        )
    )

    private val _monthSummary = MutableStateFlow(
        MonthSummary(
            totalPaise = 4250000, // ₹42,500
            previousMonthTotalPaise = 3935000, // ₹39,350
            budgetPaise = 6250000 // ₹62,500
        )
    )

    override fun observeRecent(limit: Int): Flow<List<Expense>> {
        return _expenses.map { it.take(limit) }
    }

    override fun observeMonthSummary(): Flow<MonthSummary> {
        return _monthSummary
    }

    override fun observeAnalytics(period: Period): Flow<AnalyticsSummary> {
        // TODO: Replace with SQLDelight queries
        return flowOf(AnalyticsAggregator.getSampleAnalytics(period))
    }

    override suspend fun addExpense(expense: Expense) {
        // TODO: Mark where sync engine will pick up new records
        _expenses.value = _expenses.value + expense
        
        // Update month summary
        val currentSummary = _monthSummary.value
        _monthSummary.value = currentSummary.copy(
            totalPaise = currentSummary.totalPaise + expense.amountPaise
        )
    }
}
