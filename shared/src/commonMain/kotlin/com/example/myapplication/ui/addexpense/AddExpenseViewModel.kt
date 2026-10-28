package com.example.myapplication.ui.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.ParsedExpense
import com.example.myapplication.domain.repository.ExpenseRepository
import com.example.myapplication.domain.usecase.ParseExpenseTextUseCase
import com.example.myapplication.domain.usecase.SaveExpenseUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed class ParseState {
    object Idle : ParseState()
    object Parsing : ParseState()
    data class Success(val parsed: ParsedExpense) : ParseState()
    object Error : ParseState()
}

data class AddExpenseUiState(
    val expenseId: String? = null,
    val isEditMode: Boolean = false,
    val quickText: String = "",
    val parseState: ParseState = ParseState.Idle,
    val amount: String = "",
    val category: Category? = null,
    val paymentMethod: String = "UPI",
    val date: kotlinx.datetime.LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val description: String = "",
    val amountError: String? = null,
    val categoryError: String? = null,
    val isSaving: Boolean = false,
    val manuallyEditedAmount: Boolean = false,
    val manuallyEditedCategory: Boolean = false,
    val manuallyEditedMerchant: Boolean = false
)

sealed class AddExpenseEvent {
    data class QuickTextChanged(val text: String) : AddExpenseEvent()
    object QuickTextSubmitted : AddExpenseEvent()
    data class AmountChanged(val amount: String) : AddExpenseEvent()
    data class CategorySelected(val category: Category) : AddExpenseEvent()
    data class PaymentMethodSelected(val method: String) : AddExpenseEvent()
    data class DateSelected(val date: kotlinx.datetime.LocalDate) : AddExpenseEvent()
    data class DescriptionChanged(val description: String) : AddExpenseEvent()
    object SaveClicked : AddExpenseEvent()
    object BackClicked : AddExpenseEvent()
}

class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val parseExpenseTextUseCase: ParseExpenseTextUseCase,
    private val saveExpenseUseCase: SaveExpenseUseCase,
    expenseId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState(expenseId = expenseId, isEditMode = expenseId != null))
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private var parseJob: Job? = null

    init {
        if (expenseId != null) {
            loadExpense(expenseId)
        }
    }

    private suspend fun loadExpense(id: String) {
        val expense = expenseRepository.getExpenseById(id)
        if (expense != null) {
            val timeZone = TimeZone.currentSystemDefault()
            _uiState.value = _uiState.value.copy(
                amount = (expense.amountPaise / 100).toString(),
                category = expense.category,
                paymentMethod = expense.paymentMethod,
                date = expense.timestamp.toLocalDateTime(timeZone).date,
                description = expense.merchant,
                manuallyEditedAmount = true,
                manuallyEditedCategory = true,
                manuallyEditedMerchant = true
            )
        }
    }

    fun onEvent(event: AddExpenseEvent) {
        when (event) {
            is AddExpenseEvent.QuickTextChanged -> {
                _uiState.value = _uiState.value.copy(quickText = event.text)
                debounceParse()
            }
            is AddExpenseEvent.QuickTextSubmitted -> {
                parseExpense()
            }
            is AddExpenseEvent.AmountChanged -> {
                _uiState.value = _uiState.value.copy(
                    amount = event.amount,
                    amountError = null,
                    manuallyEditedAmount = true
                )
            }
            is AddExpenseEvent.CategorySelected -> {
                _uiState.value = _uiState.value.copy(
                    category = event.category,
                    categoryError = null,
                    manuallyEditedCategory = true
                )
            }
            is AddExpenseEvent.PaymentMethodSelected -> {
                _uiState.value = _uiState.value.copy(paymentMethod = event.method)
            }
            is AddExpenseEvent.DateSelected -> {
                _uiState.value = _uiState.value.copy(date = event.date)
            }
            is AddExpenseEvent.DescriptionChanged -> {
                _uiState.value = _uiState.value.copy(
                    description = event.description,
                    manuallyEditedMerchant = true
                )
            }
            is AddExpenseEvent.SaveClicked -> {
                saveExpense()
            }
            is AddExpenseEvent.BackClicked -> {
                // Navigation handled by caller
            }
        }
    }

    private fun debounceParse() {
        parseJob?.cancel()
        parseJob = viewModelScope.launch {
            delay(700)
            parseExpense()
        }
    }

    private fun parseExpense() {
        val quickText = _uiState.value.quickText
        if (quickText.isBlank()) return

        _uiState.value = _uiState.value.copy(parseState = ParseState.Parsing)

        viewModelScope.launch {
            val result = parseExpenseTextUseCase(quickText)
            
            result.fold(
                onSuccess = { parsed ->
                    val state = _uiState.value
                    _uiState.value = state.copy(
                        parseState = ParseState.Success(parsed),
                        amount = if (!state.manuallyEditedAmount && parsed.amountPaise != null) {
                            (parsed.amountPaise / 100).toString()
                        } else state.amount,
                        category = if (!state.manuallyEditedCategory && parsed.category != null) {
                            parsed.category
                        } else state.category,
                        description = if (!state.manuallyEditedMerchant && parsed.merchant != null) {
                            parsed.merchant
                        } else state.description,
                        date = parsed.date ?: state.date
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(parseState = ParseState.Error)
                }
            )
        }
    }

    private fun saveExpense() {
        val state = _uiState.value
        
        // Validation
        val amountPaise = state.amount.toLongOrNull()?.times(100) ?: 0L
        if (amountPaise <= 0) {
            _uiState.value = state.copy(amountError = "Amount must be greater than 0")
            return
        }
        
        if (state.category == null) {
            _uiState.value = state.copy(categoryError = "Category is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            
            if (state.isEditMode && state.expenseId != null) {
                // Update existing expense
                val expense = Expense(
                    id = state.expenseId,
                    amountPaise = amountPaise,
                    category = state.category,
                    merchant = state.description.ifBlank { "Expense" },
                    paymentMethod = state.paymentMethod,
                    timestamp = state.date.atTime(12, 0).toInstant(TimeZone.currentSystemDefault())
                )
                expenseRepository.updateExpense(expense)
            } else {
                // Add new expense
                saveExpenseUseCase(
                    amountPaise = amountPaise,
                    category = state.category,
                    paymentMethod = state.paymentMethod,
                    date = state.date,
                    description = state.description.ifBlank { null }
                )
            }
            
            _uiState.value = state.copy(isSaving = false)
            // Navigation handled by caller observing success
        }
    }
}
