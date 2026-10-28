package com.example.myapplication.domain.usecase

class ValidateConfirmPasswordUseCase {
    operator fun invoke(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Please confirm your password")
        }
        if (password != confirmPassword) {
            return ValidationResult(isValid = false, errorMessage = "Passwords do not match")
        }
        return ValidationResult(isValid = true, errorMessage = null)
    }
}
