package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBudgetRepository : BudgetRepository {

    // TODO: Replace with SQLDelight and Ktor implementation
    private val _budget = MutableStateFlow(
        Budget(
            totalPaise = 62500_00L, // ₹62,500
            byCategory = mapOf(
                Category.Food to 12000_00L, // ₹12,000
                Category.Shopping to 10000_00L, // ₹10,000
                Category.Bills to 9000_00L, // ₹9,000
                Category.Transport to 5000_00L, // ₹5,000
                Category.Entertainment to 4000_00L // ₹4,000
            )
        )
    )

    override fun observeBudget(): Flow<Budget> {
        return _budget
    }

    override suspend fun save(budget: Budget): Result<Unit> {
        // TODO: Replace with SQLDelight and Ktor implementation
        _budget.value = budget
        return Result.success(Unit)
    }

    override suspend fun suggestTotal(): Long {
        // TODO: Compute AI suggestion from last 3 months of expenses
        return 58000_00L // ₹58,000
    }
}
