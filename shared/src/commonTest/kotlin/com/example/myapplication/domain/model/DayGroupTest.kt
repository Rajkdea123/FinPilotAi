package com.example.myapplication.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DayGroupTest {

    @Test
    fun `groupByDay groups expenses by date correctly`() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val yesterday = today.minus(kotlin.datetime.DatePeriod(days = 1))
        val twoDaysAgo = today.minus(kotlin.datetime.DatePeriod(days = 2))

        val expenses = listOf(
            Expense(
                id = "1",
                amountPaise = 10000,
                category = Category.Food,
                merchant = "Merchant 1",
                paymentMethod = "UPI",
                timestamp = today.atTime(12, 0).toInstant(TimeZone.currentSystemDefault())
            ),
            Expense(
                id = "2",
                amountPaise = 20000,
                category = Category.Transport,
                merchant = "Merchant 2",
                paymentMethod = "Card",
                timestamp = today.atTime(14, 0).toInstant(TimeZone.currentSystemDefault())
            ),
            Expense(
                id = "3",
                amountPaise = 15000,
                category = Category.Bills,
                merchant = "Merchant 3",
                paymentMethod = "UPI",
                timestamp = yesterday.atTime(10, 0).toInstant(TimeZone.currentSystemDefault())
            ),
            Expense(
                id = "4",
                amountPaise = 5000,
                category = Category.Shopping,
                merchant = "Merchant 4",
                paymentMethod = "Cash",
                timestamp = twoDaysAgo.atTime(15, 0).toInstant(TimeZone.currentSystemDefault())
            )
        )

        val groups = groupByDay(expenses)

        assertEquals(3, groups.size)
        
        // Check today's group
        val todayGroup = groups.find { it.date == today }
        assertTrue(todayGroup != null)
        assertEquals("Today", todayGroup?.label)
        assertEquals(30000, todayGroup?.totalPaise)
        assertEquals(2, todayGroup?.items?.size)
        
        // Check yesterday's group
        val yesterdayGroup = groups.find { it.date == yesterday }
        assertTrue(yesterdayGroup != null)
        assertEquals("Yesterday", yesterdayGroup?.label)
        assertEquals(15000, yesterdayGroup?.totalPaise)
        assertEquals(1, yesterdayGroup?.items?.size)
        
        // Check two days ago group
        val twoDaysAgoGroup = groups.find { it.date == twoDaysAgo }
        assertTrue(twoDaysAgoGroup != null)
        assertTrue(twoDaysAgoGroup?.label?.startsWith("Mon") == true || 
                   twoDaysAgoGroup?.label?.startsWith("Tue") == true ||
                   twoDaysAgoGroup?.label?.startsWith("Wed") == true ||
                   twoDaysAgoGroup?.label?.startsWith("Thu") == true ||
                   twoDaysAgoGroup?.label?.startsWith("Fri") == true ||
                   twoDaysAgoGroup?.label?.startsWith("Sat") == true ||
                   twoDaysAgoGroup?.label?.startsWith("Sun") == true)
        assertEquals(5000, twoDaysAgoGroup?.totalPaise)
        assertEquals(1, twoDaysAgoGroup?.items?.size)
    }

    @Test
    fun `groupByDay returns empty list for empty input`() {
        val groups = groupByDay(emptyList())
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `groupByDay returns single group for single expense`() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val expense = Expense(
            id = "1",
            amountPaise = 10000,
            category = Category.Food,
            merchant = "Merchant 1",
            paymentMethod = "UPI",
            timestamp = today.atTime(12, 0).toInstant(TimeZone.currentSystemDefault())
        )

        val groups = groupByDay(listOf(expense))

        assertEquals(1, groups.size)
        assertEquals(today, groups[0].date)
        assertEquals("Today", groups[0].label)
        assertEquals(10000, groups[0].totalPaise)
        assertEquals(1, groups[0].items.size)
    }

    @Test
    fun `groupByDay calculates total correctly for multiple expenses on same day`() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val expenses = listOf(
            Expense(
                id = "1",
                amountPaise = 10000,
                category = Category.Food,
                merchant = "Merchant 1",
                paymentMethod = "UPI",
                timestamp = today.atTime(12, 0).toInstant(TimeZone.currentSystemDefault())
            ),
            Expense(
                id = "2",
                amountPaise = 20000,
                category = Category.Transport,
                merchant = "Merchant 2",
                paymentMethod = "Card",
                timestamp = today.atTime(14, 0).toInstant(TimeZone.currentSystemDefault())
            ),
            Expense(
                id = "3",
                amountPaise = 15000,
                category = Category.Bills,
                merchant = "Merchant 3",
                paymentMethod = "UPI",
                timestamp = today.atTime(16, 0).toInstant(TimeZone.currentSystemDefault())
            )
        )

        val groups = groupByDay(expenses)

        assertEquals(1, groups.size)
        assertEquals(45000, groups[0].totalPaise)
        assertEquals(3, groups[0].items.size)
    }

    @Test
    fun `groupByDay sorts groups by date descending`() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val yesterday = today.minus(kotlin.datetime.DatePeriod(days = 1))
        val twoDaysAgo = today.minus(kotlin.datetime.DatePeriod(days = 2))

        val expenses = listOf(
            Expense(
                id = "1",
                amountPaise = 10000,
                category = Category.Food,
                merchant = "Merchant 1",
                paymentMethod = "UPI",
                timestamp = twoDaysAgo.atTime(12, 0).toInstant(TimeZone.currentSystemDefault())
            ),
            Expense(
                id = "2",
                amountPaise = 20000,
                category = Category.Transport,
                merchant = "Merchant 2",
                paymentMethod = "Card",
                timestamp = today.atTime(14, 0).toInstant(TimeZone.currentSystemDefault())
            ),
            Expense(
                id = "3",
                amountPaise = 15000,
                category = Category.Bills,
                merchant = "Merchant 3",
                paymentMethod = "UPI",
                timestamp = yesterday.atTime(10, 0).toInstant(TimeZone.currentSystemDefault())
            )
        )

        val groups = groupByDay(expenses)

        assertEquals(3, groups.size)
        assertEquals(today, groups[0].date)
        assertEquals(yesterday, groups[1].date)
        assertEquals(twoDaysAgo, groups[2].date)
    }
}
