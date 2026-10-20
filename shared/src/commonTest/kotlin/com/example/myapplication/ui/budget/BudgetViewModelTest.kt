package com.example.myapplication.ui.budget

import com.example.myapplication.data.repository.FakeBudgetRepository
import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.usecase.ValidateBudgetUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BudgetViewModelTest {

    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var validateBudgetUseCase: ValidateBudgetUseCase
    private lateinit var viewModel: BudgetViewModel

    @BeforeTest
    fun setup() {
        fakeBudgetRepository = FakeBudgetRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        validateBudgetUseCase = ValidateBudgetUseCase()
        viewModel = BudgetViewModel(
            fakeBudgetRepository,
            fakeExpenseRepository,
            validateBudgetUseCase
        )
    }

    @Test
    fun `initial state loads budget total and category limits`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(62500_00L, state.totalPaise)
        assertEquals(12000_00L, state.categoryLimits[Category.Food])
        assertTrue(state.isValid)
    }

    @Test
    fun `total changed updates total and validates`() = runTest {
        viewModel.onEvent(BudgetEvent.TotalChanged(70000_00L))

        val state = viewModel.uiState.value
        assertEquals(70000_00L, state.totalPaise)
        assertTrue(state.isValid)
    }

    @Test
    fun `category limit changed updates limit`() = runTest {
        viewModel.onEvent(BudgetEvent.CategoryLimitChanged(Category.Food, 15000_00L))

        val state = viewModel.uiState.value
        assertEquals(15000_00L, state.categoryLimits[Category.Food])
    }

    @Test
    fun `over assignment error invalidates state`() = runTest {
        // Set total small so category limits exceed total
        viewModel.onEvent(BudgetEvent.TotalChanged(10000_00L))

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertNotNull(state.categorySumError)
    }

    @Test
    fun `apply suggestion sets total to AI suggested amount`() = runTest {
        viewModel.onEvent(BudgetEvent.ApplySuggestion)

        val state = viewModel.uiState.value
        assertEquals(58000_00L, state.totalPaise)
    }

    @Test
    fun `save clicked when valid saves budget and sets saveSuccess`() = runTest {
        viewModel.onEvent(BudgetEvent.SaveClicked)

        val state = viewModel.uiState.value
        assertNull(state.categorySumError)
        assertTrue(state.saveSuccess)
    }
}
