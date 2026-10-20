package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.Period
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsAggregationTest {

    @Test
    fun `getSampleAnalytics returns valid week summary`() {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Week)

        assertEquals(Period.Week, summary.period)
        assertEquals(9840_00L, summary.totalPaise) // ₹9,840
        assertEquals(7, summary.points.size)
        assertEquals(4, summary.highlightIndex) // Friday
        assertEquals("Fri", summary.points[4].label)
        assertEquals(1500_00L, summary.points[4].amountPaise)
    }

    @Test
    fun `getSampleAnalytics returns valid month summary`() {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Month)

        assertEquals(Period.Month, summary.period)
        assertEquals(4, summary.points.size)
        assertEquals(1, summary.highlightIndex) // Week 2
        assertEquals(4, summary.categories.size)
        assertEquals(Category.Food, summary.categories[0].category)
        assertEquals(1.0f, summary.categories[0].ratio)
    }

    @Test
    fun `getSampleAnalytics returns valid year summary`() {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Year)

        assertEquals(Period.Year, summary.period)
        assertEquals(12, summary.points.size)
        assertEquals(9, summary.highlightIndex) // Oct
        assertEquals("Oct", summary.points[9].label)
    }

    @Test
    fun `aggregateExpenses calculates categories and ratios correctly`() {
        val now = Clock.System.now()
        val expenses = listOf(
            Expense("1", 8420_00L, Category.Food, "Zomato", "UPI", now),
            Expense("2", 4210_00L, Category.Shopping, "Amazon", "Card", now)
        )

        val summary = AnalyticsAggregator.aggregateExpenses(expenses, Period.Month)

        assertEquals(12630_00L, summary.totalPaise)
        assertEquals(2, summary.categories.size)
        assertEquals(Category.Food, summary.categories[0].category)
        assertEquals(1.0f, summary.categories[0].ratio)
        assertEquals(Category.Shopping, summary.categories[1].category)
        assertEquals(0.5f, summary.categories[1].ratio, 0.01f)
    }

    @Test
    fun `aggregateExpenses groups by week points correctly`() {
        val now = Clock.System.now()
        val expenses = listOf(
            Expense("1", 5000_00L, Category.Food, "Zomato", "UPI", now),
            Expense("2", 3000_00L, Category.Bills, "Electric", "UPI", now)
        )

        val summary = AnalyticsAggregator.aggregateExpenses(expenses, Period.Week)

        assertEquals(7, summary.points.size)
        assertEquals(8000_00L, summary.totalPaise)
    }
}
