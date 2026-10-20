package com.example.myapplication.domain.model

enum class Period {
    Week,
    Month,
    Year
}

data class SpendPoint(
    val label: String,
    val amountPaise: Long
)

data class CategorySpend(
    val category: Category,
    val amountPaise: Long,
    val ratio: Float
)

data class AnalyticsSummary(
    val period: Period,
    val totalPaise: Long,
    val points: List<SpendPoint>,
    val highlightIndex: Int,
    val categories: List<CategorySpend>
)
