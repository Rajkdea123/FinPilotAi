package com.example.myapplication.domain.usecase

class ValidatePhoneUseCase {
    operator fun invoke(phone: String): ValidationResult {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(isValid = true, errorMessage = null)
        }
        val cleanDigits = trimmed.removePrefix("+91").trim()
        if (cleanDigits.length != 10 || !cleanDigits.all { it.isDigit() }) {
            return ValidationResult(isValid = false, errorMessage = "Phone number must be 10 digits")
        }
        return ValidationResult(isValid = true, errorMessage = null)
    }
}
