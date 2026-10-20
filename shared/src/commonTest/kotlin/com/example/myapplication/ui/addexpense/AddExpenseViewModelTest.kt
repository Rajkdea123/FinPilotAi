package com.example.myapplication.ui.addexpense

import com.example.myapplication.data.ai.FakeExpenseParser
import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.usecase.ParseExpenseTextUseCase
import com.example.myapplication.domain.usecase.SaveExpenseUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {
    private lateinit var viewModel: AddExpenseViewModel
    private lateinit var fakeExpenseParser: FakeExpenseParser
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var parseExpenseTextUseCase: ParseExpenseTextUseCase
    private lateinit var saveExpenseUseCase: SaveExpenseUseCase

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeExpenseParser = FakeExpenseParser()
        fakeExpenseRepository = FakeExpenseRepository()
        parseExpenseTextUseCase = ParseExpenseTextUseCase(fakeExpenseParser)
        saveExpenseUseCase = SaveExpenseUseCase(fakeExpenseRepository)
        viewModel = AddExpenseViewModel(
            parseExpenseTextUseCase,
            saveExpenseUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle with empty fields`() {
        assertEquals("", viewModel.uiState.value.quickText)
        assertEquals(com.example.myapplication.ui.addexpense.ParseState.Idle, viewModel.uiState.value.parseState)
        assertEquals("", viewModel.uiState.value.amount)
        assertNull(viewModel.uiState.value.category)
        assertEquals("UPI", viewModel.uiState.value.paymentMethod)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `quick text change triggers parse after debounce`() = runTest {
        viewModel.onEvent(AddExpenseEvent.QuickTextChanged("Paid ₹450 at Zomato"))
        
        // Initially idle
        assertEquals(com.example.myapplication.ui.addexpense.ParseState.Parsing, viewModel.uiState.value.parseState)
        
        testDispatcher.scheduler.advanceTimeBy(700)
        
        // After debounce, should parse
        val state = viewModel.uiState.value.parseState
        assertTrue(state is com.example.myapplication.ui.addexpense.ParseState.Success)
        val parsed = (state as com.example.myapplication.ui.addexpense.ParseState.Success).parsed
        assertEquals(45000L, parsed.amountPaise)
        assertEquals(Category.Food, parsed.category)
    }

    @Test
    fun `parse fills fields when not manually edited`() = runTest {
        viewModel.onEvent(AddExpenseEvent.QuickTextChanged("Paid ₹450 at Zomato"))
        testDispatcher.scheduler.advanceTimeBy(700)
        
        assertEquals("450", viewModel.uiState.value.amount)
        assertEquals(Category.Food, viewModel.uiState.value.category)
        assertEquals("Zomato", viewModel.uiState.value.description)
    }

    @Test
    fun `parse does not overwrite manually edited amount`() = runTest {
        viewModel.onEvent(AddExpenseEvent.AmountChanged("500"))
        viewModel.onEvent(AddExpenseEvent.QuickTextChanged("Paid ₹450 at Zomato"))
        testDispatcher.scheduler.advanceTimeBy(700)
        
        assertEquals("500", viewModel.uiState.value.amount)
        assertTrue(viewModel.uiState.value.manuallyEditedAmount)
    }

    @Test
    fun `parse does not overwrite manually edited category`() = runTest {
        viewModel.onEvent(AddExpenseEvent.CategorySelected(Category.Transport))
        viewModel.onEvent(AddExpenseEvent.QuickTextChanged("Paid ₹450 at Zomato"))
        testDispatcher.scheduler.advanceTimeBy(700)
        
        assertEquals(Category.Transport, viewModel.uiState.value.category)
        assertTrue(viewModel.uiState.value.manuallyEditedCategory)
    }

    @Test
    fun `parse does not overwrite manually edited description`() = runTest {
        viewModel.onEvent(AddExpenseEvent.DescriptionChanged("Custom merchant"))
        viewModel.onEvent(AddExpenseEvent.QuickTextChanged("Paid ₹450 at Zomato"))
        testDispatcher.scheduler.advanceTimeBy(700)
        
        assertEquals("Custom merchant", viewModel.uiState.value.description)
        assertTrue(viewModel.uiState.value.manuallyEditedMerchant)
    }

    @Test
    fun `save with zero amount shows error`() = runTest {
        viewModel.onEvent(AddExpenseEvent.AmountChanged("0"))
        viewModel.onEvent(AddExpenseEvent.CategorySelected(Category.Food))
        viewModel.onEvent(AddExpenseEvent.SaveClicked)
        
        assertEquals("Amount must be greater than 0", viewModel.uiState.value.amountError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `save without category shows error`() = runTest {
        viewModel.onEvent(AddExpenseEvent.AmountChanged("450"))
        viewModel.onEvent(AddExpenseEvent.SaveClicked)
        
        assertEquals("Category is required", viewModel.uiState.value.categoryError)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `save with valid data succeeds`() = runTest {
        viewModel.onEvent(AddExpenseEvent.AmountChanged("450"))
        viewModel.onEvent(AddExpenseEvent.CategorySelected(Category.Food))
        viewModel.onEvent(AddExpenseEvent.DescriptionChanged("Zomato"))
        viewModel.onEvent(AddExpenseEvent.SaveClicked)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isSaving)
        assertNull(viewModel.uiState.value.amountError)
        assertNull(viewModel.uiState.value.categoryError)
    }

    @Test
    fun `save clears errors on valid input`() = runTest {
        // First trigger error
        viewModel.onEvent(AddExpenseEvent.AmountChanged("0"))
        viewModel.onEvent(AddExpenseEvent.CategorySelected(Category.Food))
        viewModel.onEvent(AddExpenseEvent.SaveClicked)
        assertEquals("Amount must be greater than 0", viewModel.uiState.value.amountError)
        
        // Fix the error
        viewModel.onEvent(AddExpenseEvent.AmountChanged("450"))
        viewModel.onEvent(AddExpenseEvent.SaveClicked)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertNull(viewModel.uiState.value.amountError)
    }
}
