package com.example.myapplication.ui.dashboard

import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.domain.usecase.GetAiInsightUseCase
import com.example.myapplication.domain.usecase.GetMonthSummaryUseCase
import com.example.myapplication.domain.usecase.GetRecentExpensesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private lateinit var viewModel: DashboardViewModel
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var getMonthSummaryUseCase: GetMonthSummaryUseCase
    private lateinit var getRecentExpensesUseCase: GetRecentExpensesUseCase
    private lateinit var getAiInsightUseCase: GetAiInsightUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeExpenseRepository = FakeExpenseRepository()
        getMonthSummaryUseCase = GetMonthSummaryUseCase(fakeExpenseRepository)
        getRecentExpensesUseCase = GetRecentExpensesUseCase(fakeExpenseRepository)
        getAiInsightUseCase = GetAiInsightUseCase()
        viewModel = DashboardViewModel(
            getMonthSummaryUseCase,
            getRecentExpensesUseCase,
            getAiInsightUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads data successfully`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.summary)
        assertTrue(viewModel.uiState.value.recentExpenses.isNotEmpty())
        assertTrue(viewModel.uiState.value.insight.isNotBlank())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `refresh reloads data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val initialSummary = viewModel.uiState.value.summary
        val initialExpenses = viewModel.uiState.value.recentExpenses
        
        viewModel.onEvent(DashboardEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.summary)
        assertTrue(viewModel.uiState.value.recentExpenses.isNotEmpty())
    }

    @Test
    fun `loads month summary correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val summary = viewModel.uiState.value.summary
        assertNotNull(summary)
        assertEquals(4250000, summary.totalPaise) // ₹42,500
        assertEquals(3935000, summary.previousMonthTotalPaise) // ₹39,350
        assertEquals(6250000, summary.budgetPaise) // ₹62,500
    }

    @Test
    fun `loads recent expenses correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val expenses = viewModel.uiState.value.recentExpenses
        assertTrue(expenses.isNotEmpty())
        assertEquals(3, expenses.size) // Sample data has 3 expenses
    }

    @Test
    fun `loads AI insight correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val insight = viewModel.uiState.value.insight
        assertTrue(insight.isNotBlank())
        assertTrue(insight.contains("Food"))
    }
}
