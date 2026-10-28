package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.DateRange
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.SortOrder
import com.example.myapplication.domain.model.TransactionFilter
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeExpenseRepositoryFilterSortTest {

    @Test
    fun `observeAll with no filters returns all expenses`() = runTest {
        val repository = FakeExpenseRepository()
        
        val expenses = repository.observeAll(
            filter = TransactionFilter(),
            sort = SortOrder.NewestFirst
        )
        
        // Collect the flow
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // Should return seeded expenses (40 in total)
        assertTrue(result.size >= 40)
    }

    @Test
    fun `observeAll filters by query matches merchant name`() = runTest {
        val repository = FakeExpenseRepository()
        
        val filter = TransactionFilter(query = "Zomato")
        val expenses = repository.observeAll(filter, SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // All results should contain "Zomato" in merchant name (case-insensitive)
        result.forEach { expense ->
            assertTrue(expense.merchant.contains("Zomato", ignoreCase = true))
        }
    }

    @Test
    fun `observeAll filters by category`() = runTest {
        val repository = FakeExpenseRepository()
        
        val filter = TransactionFilter(categories = setOf(Category.Food))
        val expenses = repository.observeAll(filter, SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // All results should be Food category
        result.forEach { expense ->
            assertEquals(Category.Food, expense.category)
        }
    }

    @Test
    fun `observeAll filters by multiple categories`() = runTest {
        val repository = FakeExpenseRepository()
        
        val filter = TransactionFilter(categories = setOf(Category.Food, Category.Transport))
        val expenses = repository.observeAll(filter, SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // All results should be Food or Transport
        result.forEach { expense ->
            assertTrue(expense.category == Category.Food || expense.category == Category.Transport)
        }
    }

    @Test
    fun `observeAll filters by payment method`() = runTest {
        val repository = FakeExpenseRepository()
        
        val filter = TransactionFilter(paymentMethods = setOf("UPI"))
        val expenses = repository.observeAll(filter, SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // All results should be UPI
        result.forEach { expense ->
            assertEquals("UPI", expense.paymentMethod)
        }
    }

    @Test
    fun `observeAll filters by date range`() = runTest {
        val repository = FakeExpenseRepository()
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startOfThisMonth = kotlinx.datetime.LocalDate(today.year, today.month, 1)
        val endOfThisMonth = kotlinx.datetime.LocalDate(today.year, today.month, today.month.length(today.isLeapYear))
        
        val filter = TransactionFilter(dateRange = DateRange(startOfThisMonth, endOfThisMonth))
        val expenses = repository.observeAll(filter, SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // All results should be within the date range
        val timeZone = TimeZone.currentSystemDefault()
        result.forEach { expense ->
            val localDate = expense.timestamp.toLocalDateTime(timeZone).date
            assertTrue(localDate >= startOfThisMonth && localDate <= endOfThisMonth)
        }
    }

    @Test
    fun `observeAll combines multiple filters`() = runTest {
        val repository = FakeExpenseRepository()
        
        val filter = TransactionFilter(
            categories = setOf(Category.Food),
            paymentMethods = setOf("UPI")
        )
        val expenses = repository.observeAll(filter, SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // All results should be Food AND UPI
        result.forEach { expense ->
            assertEquals(Category.Food, expense.category)
            assertEquals("UPI", expense.paymentMethod)
        }
    }

    @Test
    fun `observeAll sorts by NewestFirst`() = runTest {
        val repository = FakeExpenseRepository()
        
        val expenses = repository.observeAll(TransactionFilter(), SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // Verify descending order by timestamp
        for (i in 0 until result.size - 1) {
            assertTrue(result[i].timestamp >= result[i + 1].timestamp)
        }
    }

    @Test
    fun `observeAll sorts by OldestFirst`() = runTest {
        val repository = FakeExpenseRepository()
        
        val expenses = repository.observeAll(TransactionFilter(), SortOrder.OldestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // Verify ascending order by timestamp
        for (i in 0 until result.size - 1) {
            assertTrue(result[i].timestamp <= result[i + 1].timestamp)
        }
    }

    @Test
    fun `observeAll sorts by HighestAmount`() = runTest {
        val repository = FakeExpenseRepository()
        
        val expenses = repository.observeAll(TransactionFilter(), SortOrder.HighestAmount)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // Verify descending order by amount
        for (i in 0 until result.size - 1) {
            assertTrue(result[i].amountPaise >= result[i + 1].amountPaise)
        }
    }

    @Test
    fun `observeAll with empty query returns all expenses`() = runTest {
        val repository = FakeExpenseRepository()
        
        val filter = TransactionFilter(query = "")
        val expenses = repository.observeAll(filter, SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // Should return all expenses
        val allExpenses = repository.observeAll(TransactionFilter(), SortOrder.NewestFirst)
        val allResult = kotlinx.coroutines.flow.first(allExpenses)
        assertEquals(allResult.size, result.size)
    }

    @Test
    fun `observeAll with blank query returns all expenses`() = runTest {
        val repository = FakeExpenseRepository()
        
        val filter = TransactionFilter(query = "   ")
        val expenses = repository.observeAll(filter, SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(expenses)
        
        // Should return all expenses
        val allExpenses = repository.observeAll(TransactionFilter(), SortOrder.NewestFirst)
        val allResult = kotlinx.coroutines.flow.first(allExpenses)
        assertEquals(allResult.size, result.size)
    }
}
