package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Category

data class BudgetValidationResult(
    val isValid: Boolean,
    val totalError: String? = null,
    val categorySumError: String? = null
)

class ValidateBudgetUseCase {
    operator fun invoke(totalPaise: Long, categoryLimits: Map<Category, Long>): BudgetValidationResult {
        if (totalPaise <= 0L) {
            return BudgetValidationResult(
                isValid = false,
                totalError = "Total budget must be greater than 0"
            )
        }

        val sumCategoryLimits = categoryLimits.values.sum()
        if (sumCategoryLimits > totalPaise) {
            return BudgetValidationResult(
                isValid = false,
                categorySumError = "Sum of category limits cannot exceed total budget"
            )
        }

        return BudgetValidationResult(isValid = true)
    }
}
