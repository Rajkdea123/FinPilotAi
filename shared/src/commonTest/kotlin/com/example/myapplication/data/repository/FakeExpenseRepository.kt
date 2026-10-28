package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.DateRange
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.model.SortOrder
import com.example.myapplication.domain.model.TransactionFilter
import com.example.myapplication.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class FakeExpenseRepository : ExpenseRepository {
    private val _expenses = MutableStateFlow(generateSeedExpenses())
    private val _deletedExpenses = MutableStateFlow<List<Expense>>(emptyList())

    private val _monthSummary = MutableStateFlow(
        MonthSummary(
            totalPaise = 4250000, // ₹42,500
            previousMonthTotalPaise = 3935000, // ₹39,350
            budgetPaise = 6250000 // ₹62,500
        )
    )

    override fun observeRecent(limit: Int): Flow<List<Expense>> {
        return _expenses.map { expenses ->
            expenses.sortedByDescending { it.timestamp }.take(limit)
        }
    }

    override fun observeMonthSummary(): Flow<MonthSummary> {
        return _monthSummary
    }

    override fun observeAnalytics(period: Period): Flow<AnalyticsSummary> {
        // TODO: Replace with SQLDelight queries
        return flowOf(AnalyticsAggregator.getSampleAnalytics(period))
    }

    override fun observeAll(filter: TransactionFilter, sort: SortOrder): Flow<List<Expense>> {
        // TODO: Replace with SQLDelight queries for filtering and sorting
        return _expenses.map { expenses ->
            var filtered = expenses

            // Filter by query
            if (filter.query.isNotBlank()) {
                filtered = filtered.filter { expense ->
                    expense.merchant.contains(filter.query, ignoreCase = true)
                }
            }

            // Filter by categories
            if (filter.categories.isNotEmpty()) {
                filtered = filtered.filter { it.category in filter.categories }
            }

            // Filter by payment methods
            if (filter.paymentMethods.isNotEmpty()) {
                filtered = filtered.filter { it.paymentMethod in filter.paymentMethods }
            }

            // Filter by date range
            if (filter.dateRange != null) {
                val timeZone = TimeZone.currentSystemDefault()
                filtered = filtered.filter { expense ->
                    val localDate = expense.timestamp.toLocalDateTime(timeZone).date
                    localDate >= filter.dateRange!!.start && localDate <= filter.dateRange!!.end
                }
            }

            // Sort
            when (sort) {
                SortOrder.NewestFirst -> filtered.sortedByDescending { it.timestamp }
                SortOrder.OldestFirst -> filtered.sortedBy { it.timestamp }
                SortOrder.HighestAmount -> filtered.sortedByDescending { it.amountPaise }
            }
        }
    }

    override suspend fun getExpenseById(id: String): Expense? {
        return _expenses.value.find { it.id == id }
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

    override suspend fun updateExpense(expense: Expense) {
        // TODO: Mark where sync engine will pick up updated records
        _expenses.value = _expenses.value.map {
            if (it.id == expense.id) expense else it
        }
        
        // Update month summary (recalculate for simplicity)
        val newTotal = _expenses.value.sumOf { it.amountPaise }
        _monthSummary.value = _monthSummary.value.copy(totalPaise = newTotal)
    }

    override suspend fun delete(id: String) {
        // TODO: Mark as deleted (tombstone) instead of removing for sync engine
        val expense = _expenses.value.find { it.id == id }
        if (expense != null) {
            _expenses.value = _expenses.value.filter { it.id != id }
            _deletedExpenses.value = _deletedExpenses.value + expense
            
            // Update month summary
            val currentSummary = _monthSummary.value
            _monthSummary.value = currentSummary.copy(
                totalPaise = currentSummary.totalPaise - expense.amountPaise
            )
        }
    }

    override suspend fun restore(expense: Expense) {
        // TODO: Remove tombstone for sync engine
        _expenses.value = _expenses.value + expense
        _deletedExpenses.value = _deletedExpenses.value.filter { it.id != expense.id }
        
        // Update month summary
        val currentSummary = _monthSummary.value
        _monthSummary.value = currentSummary.copy(
            totalPaise = currentSummary.totalPaise + expense.amountPaise
        )
    }

    private fun generateSeedExpenses(): List<Expense> {
        val now = Clock.System.now()
        val timeZone = TimeZone.currentSystemDefault()
        val merchants = listOf(
            "Zomato", "Swiggy", "Uber", "Metro", "Amazon", "Flipkart",
            "Netflix", "Spotify", "Electricity", "Rent", "Grocery Store",
            "Starbucks", "McDonald's", "Petrol Pump", "Pharmacy", "Gym"
        )
        val categories = Category.values()
        val paymentMethods = listOf("UPI", "Card", "Cash", "Bank Transfer")
        
        val expenses = mutableListOf<Expense>()
        var idCounter = 1

        // Generate expenses for the last 3 months
        for (monthOffset in 0 until 3) {
            for (day in 1 until 15) {
                val daysAgo = (monthOffset * 30) + day
                val timestamp = now.minus(DateTimePeriod(days = daysAgo))
                
                repeat(1) {
                    val merchant = merchants[(idCounter % merchants.size)]
                    val category = categories[(idCounter % categories.size)]
                    val paymentMethod = paymentMethods[(idCounter % paymentMethods.size)]
                    val amount = (1000 + (idCounter * 500) % 10000) * 100L // ₹100 to ₹10,000
                    
                    expenses.add(
                        Expense(
                            id = idCounter.toString(),
                            amountPaise = amount,
                            category = category,
                            merchant = merchant,
                            paymentMethod = paymentMethod,
                            timestamp = timestamp
                        )
                    )
                    idCounter++
                }
            }
        }

        return expenses
    }
}
