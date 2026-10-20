package com.example.myapplication.platform.biometric

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class BiometricAuthenticator {
    // TODO: Implement using LocalAuthentication framework
    actual suspend fun authenticate(): BiometricResult {
        return BiometricResult.Error("Not implemented yet")
    }

    actual val isAvailable: Flow<Boolean> = flowOf(false)
}
