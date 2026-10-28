package com.example.myapplication.domain.model

enum class PasswordStrength {
    Weak,
    Fair,
    Strong
}

fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.length < 8) return PasswordStrength.Weak
    
    var varietyScore = 0
    if (password.any { it.isLowerCase() }) varietyScore++
    if (password.any { it.isUpperCase() }) varietyScore++
    if (password.any { it.isDigit() }) varietyScore++
    if (password.any { !it.isLetterOrDigit() }) varietyScore++
    
    return when {
        varietyScore >= 3 -> PasswordStrength.Strong
        varietyScore >= 2 -> PasswordStrength.Fair
        else -> PasswordStrength.Weak
    }
}
