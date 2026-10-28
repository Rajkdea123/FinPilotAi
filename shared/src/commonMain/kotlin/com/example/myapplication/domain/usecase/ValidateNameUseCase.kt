package com.example.myapplication.domain.usecase

class ValidateNameUseCase {
    operator fun invoke(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Name is required")
        }
        if (name.length < 2) {
            return ValidationResult(isValid = false, errorMessage = "Name must be at least 2 characters")
        }
        return ValidationResult(isValid = true, errorMessage = null)
    }
}
