package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.CategorySpend
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.model.SpendPoint
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object AnalyticsAggregator {

    fun getSampleAnalytics(period: Period): AnalyticsSummary {
        // TODO: Replace with SQLDelight queries
        val categories = listOf(
            CategorySpend(Category.Food, 8420_00L, 1.0f),
            CategorySpend(Category.Shopping, 6100_00L, 6100f / 8420f),
            CategorySpend(Category.Bills, 5300_00L, 5300f / 8420f),
            CategorySpend(Category.Transport, 3000_00L, 3000f / 8420f)
        )

        return when (period) {
            Period.Week -> AnalyticsSummary(
                period = Period.Week,
                totalPaise = 9840_00L, // ₹9,840
                points = listOf(
                    SpendPoint("Mon", 700_00L),
                    SpendPoint("Tue", 1100_00L),
                    SpendPoint("Wed", 600_00L),
                    SpendPoint("Thu", 900_00L),
                    SpendPoint("Fri", 1500_00L),
                    SpendPoint("Sat", 1200_00L),
                    SpendPoint("Sun", 800_00L)
                ),
                highlightIndex = 4, // Friday
                categories = categories
            )

            Period.Month -> AnalyticsSummary(
                period = Period.Month,
                totalPaise = 22820_00L, // ₹22,820
                points = listOf(
                    SpendPoint("Week 1", 5200_00L),
                    SpendPoint("Week 2", 6800_00L),
                    SpendPoint("Week 3", 4500_00L),
                    SpendPoint("Week 4", 6320_00L)
                ),
                highlightIndex = 1, // Week 2
                categories = categories
            )

            Period.Year -> AnalyticsSummary(
                period = Period.Year,
                totalPaise = 273840_00L, // ₹2,73,840
                points = listOf(
                    SpendPoint("Jan", 18000_00L),
                    SpendPoint("Feb", 19500_00L),
                    SpendPoint("Mar", 21000_00L),
                    SpendPoint("Apr", 17500_00L),
                    SpendPoint("May", 22000_00L),
                    SpendPoint("Jun", 25000_00L),
                    SpendPoint("Jul", 20500_00L),
                    SpendPoint("Aug", 24000_00L),
                    SpendPoint("Sep", 22840_00L),
                    SpendPoint("Oct", 26000_00L),
                    SpendPoint("Nov", 23500_00L),
                    SpendPoint("Dec", 28000_00L)
                ),
                highlightIndex = 9, // Oct
                categories = categories
            )
        }
    }

    fun aggregateExpenses(expenses: List<Expense>, period: Period): AnalyticsSummary {
        val totalPaise = expenses.sumOf { it.amountPaise }
        
        // Group expenses by category
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amountPaise } }

        val maxCategoryAmount = categoryTotals.values.maxOrNull() ?: 0L

        val categories = Category.entries.mapNotNull { category ->
            val amount = categoryTotals[category] ?: 0L
            if (amount > 0L) {
                val ratio = if (maxCategoryAmount > 0L) amount.toFloat() / maxCategoryAmount.toFloat() else 0f
                CategorySpend(category, amount, ratio)
            } else null
        }.sortedByDescending { it.amountPaise }

        // Points depending on period
        val timeZone = kotlinx.datetime.TimeZone.UTC
        val points: List<SpendPoint> = when (period) {
            Period.Week -> {
                val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                labels.mapIndexed { idx, label ->
                    val amount = expenses.filter { 
                        val dateTime = it.timestamp.toLocalDateTime(timeZone)
                        dateTime.dayOfWeek.ordinal == idx 
                    }.sumOf { it.amountPaise }
                    SpendPoint(label, amount)
                }
            }
            Period.Month -> {
                val labels = listOf("Week 1", "Week 2", "Week 3", "Week 4")
                labels.mapIndexed { idx, label ->
                    val amount = expenses.filter { 
                        val dateTime = it.timestamp.toLocalDateTime(timeZone)
                        val weekIdx = ((dateTime.dayOfMonth - 1) / 7).coerceAtMost(3)
                        weekIdx == idx 
                    }.sumOf { it.amountPaise }
                    SpendPoint(label, amount)
                }
            }
            Period.Year -> {
                val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                labels.mapIndexed { idx, label ->
                    val amount = expenses.filter { 
                        val dateTime = it.timestamp.toLocalDateTime(timeZone)
                        dateTime.month.ordinal == idx
                    }.sumOf { it.amountPaise }
                    SpendPoint(label, amount)
                }
            }
        }

        val maxPointIdx = points.indices.maxByOrNull { points[it].amountPaise } ?: 0

        return AnalyticsSummary(
            period = period,
            totalPaise = totalPaise,
            points = points,
            highlightIndex = maxPointIdx,
            categories = categories
        )
    }
}
