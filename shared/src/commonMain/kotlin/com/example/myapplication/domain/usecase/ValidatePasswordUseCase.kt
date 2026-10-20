package com.example.myapplication.domain.usecase

class ValidatePasswordUseCase {
    operator fun invoke(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Password is required")
        }
        
        if (password.length < 8) {
            return ValidationResult(isValid = false, errorMessage = "Password must be at least 8 characters")
        }
        
        return ValidationResult(isValid = true, errorMessage = null)
    }
}
