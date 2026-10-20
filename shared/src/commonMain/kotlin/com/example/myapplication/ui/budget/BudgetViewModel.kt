package com.example.myapplication.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.BudgetRepository
import com.example.myapplication.domain.repository.ExpenseRepository
import com.example.myapplication.domain.usecase.ValidateBudgetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetUiState(
    val totalPaise: Long = 62500_00L,
    val categoryLimits: Map<Category, Long> = mapOf(
        Category.Food to 12000_00L,
        Category.Shopping to 10000_00L,
        Category.Bills to 9000_00L,
        Category.Transport to 5000_00L,
        Category.Entertainment to 4000_00L
    ),
    val categorySpent: Map<Category, Long> = mapOf(
        Category.Food to 8420_00L,
        Category.Shopping to 6100_00L,
        Category.Bills to 5300_00L,
        Category.Transport to 3000_00L,
        Category.Entertainment to 2000_00L
    ),
    val aiSuggestionPaise: Long = 58000_00L,
    val totalError: String? = null,
    val categorySumError: String? = null,
    val isValid: Boolean = true,
    val isSaving: Boolean = false,
    val isEditingTotal: Boolean = false,
    val editingCategory: Category? = null,
    val saveSuccess: Boolean = false
)

sealed interface BudgetEvent {
    data class TotalChanged(val totalPaise: Long) : BudgetEvent
    data class CategoryLimitChanged(val category: Category, val limitPaise: Long) : BudgetEvent
    object ApplySuggestion : BudgetEvent
    object EditTotalClicked : BudgetEvent
    object DismissEditTotal : BudgetEvent
    data class EditCategoryClicked(val category: Category) : BudgetEvent
    object DismissEditCategory : BudgetEvent
    object SaveClicked : BudgetEvent
}

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val validateBudgetUseCase: ValidateBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val budget = budgetRepository.observeBudget().firstOrNull()
            val aiSuggestion = budgetRepository.suggestTotal()

            if (budget != null) {
                _uiState.update {
                    it.copy(
                        totalPaise = budget.totalPaise,
                        categoryLimits = budget.byCategory,
                        aiSuggestionPaise = aiSuggestion
                    )
                }
                validate()
            }
        }
    }

    fun onEvent(event: BudgetEvent) {
        when (event) {
            is BudgetEvent.TotalChanged -> {
                _uiState.update { it.copy(totalPaise = event.totalPaise) }
                validate()
            }

            is BudgetEvent.CategoryLimitChanged -> {
                _uiState.update {
                    val updatedLimits = it.categoryLimits.toMutableMap()
                    updatedLimits[event.category] = event.limitPaise
                    it.copy(categoryLimits = updatedLimits)
                }
                validate()
            }

            BudgetEvent.ApplySuggestion -> {
                val suggestion = _uiState.value.aiSuggestionPaise
                _uiState.update { it.copy(totalPaise = suggestion) }
                validate()
            }

            BudgetEvent.EditTotalClicked -> {
                _uiState.update { it.copy(isEditingTotal = true) }
            }

            BudgetEvent.DismissEditTotal -> {
                _uiState.update { it.copy(isEditingTotal = false) }
            }

            is BudgetEvent.EditCategoryClicked -> {
                _uiState.update { it.copy(editingCategory = event.category) }
            }

            BudgetEvent.DismissEditCategory -> {
                _uiState.update { it.copy(editingCategory = null) }
            }

            BudgetEvent.SaveClicked -> {
                saveBudget()
            }
        }
    }

    private fun validate() {
        val state = _uiState.value
        val result = validateBudgetUseCase(state.totalPaise, state.categoryLimits)
        _uiState.update {
            it.copy(
                isValid = result.isValid,
                totalError = result.totalError,
                categorySumError = result.categorySumError
            )
        }
    }

    private fun saveBudget() {
        validate()
        if (!_uiState.value.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val current = _uiState.value
            val result = budgetRepository.save(
                Budget(
                    totalPaise = current.totalPaise,
                    byCategory = current.categoryLimits
                )
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } else {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
