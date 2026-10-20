package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.domain.usecase.GetAiInsightUseCase
import com.example.myapplication.domain.usecase.GetMonthSummaryUseCase
import com.example.myapplication.domain.usecase.GetRecentExpensesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: MonthSummary? = null,
    val recentExpenses: List<Expense> = emptyList(),
    val insight: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class DashboardEvent {
    object Refresh : DashboardEvent()
}

class DashboardViewModel(
    private val getMonthSummaryUseCase: GetMonthSummaryUseCase,
    private val getRecentExpensesUseCase: GetRecentExpensesUseCase,
    private val getAiInsightUseCase: GetAiInsightUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.Refresh -> {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                combine(
                    getMonthSummaryUseCase(),
                    getRecentExpensesUseCase(10)
                ) { summary, expenses ->
                    Pair(summary, expenses)
                }.collect { (summary, expenses) ->
                    _uiState.value = DashboardUiState(
                        summary = summary,
                        recentExpenses = expenses,
                        insight = getAiInsightUseCase(),
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }
}
