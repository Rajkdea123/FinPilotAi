package com.example.myapplication.platform.biometric

import kotlinx.coroutines.flow.Flow

sealed class BiometricResult {
    object Success : BiometricResult()
    object Failed : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

expect class BiometricAuthenticator {
    suspend fun authenticate(): BiometricResult
    val isAvailable: Flow<Boolean>
}
