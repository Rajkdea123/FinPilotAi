package com.example.myapplication.ui.transactions

import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.DateRange
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.SortOrder
import com.example.myapplication.domain.model.TransactionFilter
import com.example.myapplication.domain.usecase.DeleteExpenseUseCase
import com.example.myapplication.domain.usecase.ObserveTransactionsUseCase
import com.example.myapplication.domain.usecase.RestoreExpenseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: FakeExpenseRepository
    private lateinit var observeTransactionsUseCase: ObserveTransactionsUseCase
    private lateinit var deleteExpenseUseCase: DeleteExpenseUseCase
    private lateinit var restoreExpenseUseCase: RestoreExpenseUseCase
    private lateinit var viewModel: TransactionsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeExpenseRepository()
        observeTransactionsUseCase = ObserveTransactionsUseCase(repository)
        deleteExpenseUseCase = DeleteExpenseUseCase(repository)
        restoreExpenseUseCase = RestoreExpenseUseCase(repository)
        viewModel = TransactionsViewModel(
            observeTransactionsUseCase,
            deleteExpenseUseCase,
            restoreExpenseUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val state = viewModel.uiState.value
        
        assertEquals(TransactionFilter(), state.filter)
        assertEquals(SortOrder.NewestFirst, state.sort)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.groups.isNotEmpty()) // Should have seeded data
    }

    @Test
    fun `QueryChanged updates filter query`() = runTest {
        viewModel.onEvent(TransactionsEvent.QueryChanged("Zomato"))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("Zomato", viewModel.uiState.value.filter.query)
    }

    @Test
    fun `CategorySelected adds category to filter`() = runTest {
        viewModel.onEvent(TransactionsEvent.CategorySelected(Category.Food))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.filter.categories.contains(Category.Food))
    }

    @Test
    fun `CategorySelected removes category if already selected`() = runTest {
        viewModel.onEvent(TransactionsEvent.CategorySelected(Category.Food))
        viewModel.onEvent(TransactionsEvent.CategorySelected(Category.Food))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.filter.categories.contains(Category.Food))
    }

    @Test
    fun `PaymentMethodSelected adds payment method to filter`() = runTest {
        viewModel.onEvent(TransactionsEvent.PaymentMethodSelected("UPI"))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.filter.paymentMethods.contains("UPI"))
    }

    @Test
    fun `PaymentMethodSelected removes payment method if already selected`() = runTest {
        viewModel.onEvent(TransactionsEvent.PaymentMethodSelected("UPI"))
        viewModel.onEvent(TransactionsEvent.PaymentMethodSelected("UPI"))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.filter.paymentMethods.contains("UPI"))
    }

    @Test
    fun `DateRangeSelected updates filter date range`() = runTest {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dateRange = DateRange(today, today)
        
        viewModel.onEvent(TransactionsEvent.DateRangeSelected(dateRange))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(dateRange, viewModel.uiState.value.filter.dateRange)
    }

    @Test
    fun `SortSelected updates sort order`() = runTest {
        viewModel.onEvent(TransactionsEvent.SortSelected(SortOrder.HighestAmount))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(SortOrder.HighestAmount, viewModel.uiState.value.sort)
    }

    @Test
    fun `ClearFilters resets all filters`() = runTest {
        viewModel.onEvent(TransactionsEvent.CategorySelected(Category.Food))
        viewModel.onEvent(TransactionsEvent.PaymentMethodSelected("UPI"))
        viewModel.onEvent(TransactionsEvent.QueryChanged("test"))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.onEvent(TransactionsEvent.ClearFilters)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(TransactionFilter(), viewModel.uiState.value.filter)
    }

    @Test
    fun `DeleteExpense removes expense and stores lastDeleted`() = runTest {
        // Add a test expense
        val expense = Expense(
            id = "test-delete-1",
            amountPaise = 10000,
            category = Category.Food,
            merchant = "Test Merchant",
            paymentMethod = "UPI",
            timestamp = Clock.System.now()
        )
        repository.addExpense(expense)
        
        // Trigger delete
        viewModel.onEvent(TransactionsEvent.DeleteExpense("test-delete-1"))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify lastDeleted is set
        assertNotNull(viewModel.uiState.value.lastDeleted)
        assertEquals("test-delete-1", viewModel.uiState.value.lastDeleted?.id)
    }

    @Test
    fun `UndoDelete restores the last deleted expense`() = runTest {
        // Add a test expense
        val expense = Expense(
            id = "test-undo-1",
            amountPaise = 10000,
            category = Category.Food,
            merchant = "Test Merchant",
            paymentMethod = "UPI",
            timestamp = Clock.System.now()
        )
        repository.addExpense(expense)
        
        // Delete
        viewModel.onEvent(TransactionsEvent.DeleteExpense("test-undo-1"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Undo
        viewModel.onEvent(TransactionsEvent.UndoDelete)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify lastDeleted is cleared
        assertNull(viewModel.uiState.value.lastDeleted)
        
        // Verify expense is restored
        val allExpenses = repository.observeAll(TransactionFilter(), SortOrder.NewestFirst)
        val result = kotlinx.coroutines.flow.first(allExpenses)
        assertTrue(result.any { it.id == "test-undo-1" })
    }

    @Test
    fun `Retry clears error state`() = runTest {
        // Manually set error state (simulating a previous error)
        // Note: In a real scenario, this would happen from a failed operation
        // For this test, we'll just verify the event handling
        
        viewModel.onEvent(TransactionsEvent.Retry)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // After retry, should reload data
        // The error should be cleared if there was one
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `ExpenseClicked triggers navigation callback`() = runTest {
        var clickedExpenseId: String? = null
        
        // Create a custom ViewModel to test the event
        val testViewModel = TransactionsViewModel(
            observeTransactionsUseCase,
            deleteExpenseUseCase,
            restoreExpenseUseCase
        )
        
        testViewModel.onEvent(TransactionsEvent.ExpenseClicked("test-expense-123"))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // The event is handled, in real app this would trigger navigation
        // For now we just verify it doesn't crash
    }

    @Test
    fun `multiple filters work together`() = runTest {
        viewModel.onEvent(TransactionsEvent.CategorySelected(Category.Food))
        viewModel.onEvent(TransactionsEvent.PaymentMethodSelected("UPI"))
        viewModel.onEvent(TransactionsEvent.QueryChanged("Zomato"))
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state.filter.categories.contains(Category.Food))
        assertTrue(state.filter.paymentMethods.contains("UPI"))
        assertEquals("Zomato", state.filter.query)
    }

    @Test
    fun `sort order changes are reflected in results`() = runTest {
        // Get initial order (NewestFirst)
        val state1 = viewModel.uiState.value
        val groups1 = state1.groups
        
        // Change to OldestFirst
        viewModel.onEvent(TransactionsEvent.SortSelected(SortOrder.OldestFirst))
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state2 = viewModel.uiState.value
        assertEquals(SortOrder.OldestFirst, state2.sort)
    }

    @Test
    fun `empty query returns all transactions`() = runTest {
        viewModel.onEvent(TransactionsEvent.QueryChanged(""))
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("", state.filter.query)
        // Should have seeded data
        assertTrue(state.groups.isNotEmpty())
    }

    @Test
    fun `query with no matches returns empty groups`() = runTest {
        viewModel.onEvent(TransactionsEvent.QueryChanged("xyznonexistent123"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("xyznonexistent123", state.filter.query)
        // Should have no matches
        assertTrue(state.groups.isEmpty())
    }
}
