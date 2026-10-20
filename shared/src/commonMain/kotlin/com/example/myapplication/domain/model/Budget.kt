package com.example.myapplication.domain.model

data class Budget(
    val totalPaise: Long,
    val byCategory: Map<Category, Long>
)

data class BudgetUsage(
    val category: Category,
    val limitPaise: Long,
    val spentPaise: Long
)
