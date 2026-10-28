package com.example.myapplication.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.DateRange
import com.example.myapplication.domain.model.DayGroup
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.SortOrder
import com.example.myapplication.domain.model.TransactionFilter
import com.example.myapplication.domain.usecase.DeleteExpenseUseCase
import com.example.myapplication.domain.usecase.ObserveTransactionsUseCase
import com.example.myapplication.domain.usecase.RestoreExpenseUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val groups: List<DayGroup> = emptyList(),
    val filter: TransactionFilter = TransactionFilter(),
    val sort: SortOrder = SortOrder.NewestFirst,
    val resultCount: Int = 0,
    val totalPaise: Long = 0L,
    val isLoading: Boolean = true,
    val error: String? = null,
    val lastDeleted: Expense? = null
)

sealed class TransactionsEvent {
    data class QueryChanged(val query: String) : TransactionsEvent()
    data class CategoryToggled(val category: com.example.myapplication.domain.model.Category) : TransactionsEvent()
    data class PaymentMethodToggled(val paymentMethod: String) : TransactionsEvent()
    data class DateRangeSelected(val dateRange: DateRange?) : TransactionsEvent()
    data class SortSelected(val sort: SortOrder) : TransactionsEvent()
    object ClearFilters : TransactionsEvent()
    data class ExpenseClicked(val id: String) : TransactionsEvent()
    data class DeleteExpense(val id: String) : TransactionsEvent()
    object UndoDelete : TransactionsEvent()
    object Retry : TransactionsEvent()
    object BackClicked : TransactionsEvent()
}

class TransactionsViewModel(
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val restoreExpenseUseCase: RestoreExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTransactions()
    }

    fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.QueryChanged -> {
                _uiState.value = _uiState.value.copy(
                    filter = _uiState.value.filter.copy(query = event.query)
                )
                debounceSearch()
            }
            is TransactionsEvent.CategoryToggled -> {
                val categories = _uiState.value.filter.categories.toMutableSet()
                if (event.category in categories) {
                    categories.remove(event.category)
                } else {
                    categories.add(event.category)
                }
                _uiState.value = _uiState.value.copy(
                    filter = _uiState.value.filter.copy(categories = categories)
                )
                loadTransactions()
            }
            is TransactionsEvent.PaymentMethodToggled -> {
                val paymentMethods = _uiState.value.filter.paymentMethods.toMutableSet()
                if (event.paymentMethod in paymentMethods) {
                    paymentMethods.remove(event.paymentMethod)
                } else {
                    paymentMethods.add(event.paymentMethod)
                }
                _uiState.value = _uiState.value.copy(
                    filter = _uiState.value.filter.copy(paymentMethods = paymentMethods)
                )
                loadTransactions()
            }
            is TransactionsEvent.DateRangeSelected -> {
                _uiState.value = _uiState.value.copy(
                    filter = _uiState.value.filter.copy(dateRange = event.dateRange)
                )
                loadTransactions()
            }
            is TransactionsEvent.SortSelected -> {
                _uiState.value = _uiState.value.copy(sort = event.sort)
                loadTransactions()
            }
            is TransactionsEvent.ClearFilters -> {
                _uiState.value = _uiState.value.copy(
                    filter = TransactionFilter(),
                    sort = SortOrder.NewestFirst
                )
                loadTransactions()
            }
            is TransactionsEvent.ExpenseClicked -> {
                // Navigation handled by observing this event
            }
            is TransactionsEvent.DeleteExpense -> {
                deleteExpense(event.id)
            }
            is TransactionsEvent.UndoDelete -> {
                undoDelete()
            }
            is TransactionsEvent.Retry -> {
                loadTransactions()
            }
            is TransactionsEvent.BackClicked -> {
                // Navigation handled by observing this event
            }
        }
    }

    private fun debounceSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadTransactions()
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                observeTransactionsUseCase(_uiState.value.filter, _uiState.value.sort)
                    .collect { groups ->
                        val totalCount = groups.sumOf { it.items.size }
                        val totalAmount = groups.sumOf { it.totalPaise }
                        _uiState.value = _uiState.value.copy(
                            groups = groups,
                            resultCount = totalCount,
                            totalPaise = totalAmount,
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load transactions"
                )
            }
        }
    }

    private fun deleteExpense(id: String) {
        viewModelScope.launch {
            val expense = _uiState.value.groups.flatMap { it.items }.find { it.id == id }
            if (expense != null) {
                deleteExpenseUseCase(id)
                _uiState.value = _uiState.value.copy(lastDeleted = expense)
            }
        }
    }

    private fun undoDelete() {
        viewModelScope.launch {
            val lastDeleted = _uiState.value.lastDeleted
            if (lastDeleted != null) {
                restoreExpenseUseCase(lastDeleted)
                _uiState.value = _uiState.value.copy(lastDeleted = null)
            }
        }
    }
}
