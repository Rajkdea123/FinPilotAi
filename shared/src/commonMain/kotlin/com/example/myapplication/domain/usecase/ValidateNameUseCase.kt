package com.example.myapplication.domain.usecase

class ValidateNameUseCase {
    operator fun invoke(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Name cannot be blank")
        }
        return ValidationResult(isValid = true, errorMessage = null)
    }
}
