package com.example.myapplication.domain.usecase

class ValidateEmailUseCase {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Email is required")
        }
        
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(email)) {
            return ValidationResult(isValid = false, errorMessage = "Invalid email format")
        }
        
        return ValidationResult(isValid = true, errorMessage = null)
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)
