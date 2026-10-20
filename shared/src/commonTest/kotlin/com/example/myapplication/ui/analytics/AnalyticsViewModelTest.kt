package com.example.myapplication.ui.analytics

import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.repository.ExpenseRepository
import com.example.myapplication.domain.usecase.GetAnalyticsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var getAnalyticsUseCase: GetAnalyticsUseCase
    private lateinit var viewModel: AnalyticsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeExpenseRepository = FakeExpenseRepository()
        getAnalyticsUseCase = GetAnalyticsUseCase(fakeExpenseRepository)
        viewModel = AnalyticsViewModel(getAnalyticsUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state defaults to Month period and loads analytics`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Period.Month, state.period)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.summary)
        assertEquals(1, state.selectedIndex) // Month highlight index = 1
    }

    @Test
    fun `period selected switches period and resets selectedIndex to highlight`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AnalyticsEvent.PeriodSelected(Period.Week))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Period.Week, state.period)
        assertFalse(state.isLoading)
        assertNotNull(state.summary)
        assertEquals(Period.Week, state.summary!!.period)
        assertEquals(4, state.selectedIndex) // Week highlight index = 4 (Friday)
    }

    @Test
    fun `bar selected updates selectedIndex`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AnalyticsEvent.BarSelected(0))

        val state = viewModel.uiState.value
        assertEquals(0, state.selectedIndex)
    }

    @Test
    fun `bar selected with invalid index is ignored`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val initialSelectedIndex = viewModel.uiState.value.selectedIndex

        viewModel.onEvent(AnalyticsEvent.BarSelected(100))

        val state = viewModel.uiState.value
        assertEquals(initialSelectedIndex, state.selectedIndex)
    }

    @Test
    fun `error scenario and retry flow`() = runTest {
        val errorRepository = object : ExpenseRepository {
            var shouldFail = true
            override fun observeRecent(limit: Int): Flow<List<Expense>> = flow { }
            override fun observeMonthSummary(): Flow<MonthSummary> = flow { }
            override fun observeAnalytics(period: Period): Flow<AnalyticsSummary> = flow {
                if (shouldFail) {
                    throw IllegalStateException("Network error")
                } else {
                    emit(AnalyticsSummary(period, 1000L, emptyList(), 0, emptyList()))
                }
            }
            override suspend fun addExpense(expense: Expense) {}
        }

        val errorViewModel = AnalyticsViewModel(GetAnalyticsUseCase(errorRepository))
        testDispatcher.scheduler.advanceUntilIdle()

        var state = errorViewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)

        // Now retry with shouldFail = false
        errorRepository.shouldFail = false
        errorViewModel.onEvent(AnalyticsEvent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        state = errorViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNotNull(state.summary)
    }
}
