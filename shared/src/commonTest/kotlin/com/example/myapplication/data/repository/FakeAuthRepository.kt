package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.repository.AuthRepository
import kotlinx.coroutines.delay

class FakeAuthRepository : AuthRepository {
    override suspend fun signIn(email: String, password: String): AuthResult {
        // TODO: Replace with Ktor + JWT implementation
        delay(1200)
        return AuthResult.Success
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        // TODO: Replace with Ktor + JWT implementation
        delay(1200)
        
        // Simulate email already taken
        if (email == "taken@example.com") {
            return AuthResult.Error("Email already in use")
        }
        
        return AuthResult.Success
    }

    override suspend fun requestPasswordReset(email: String): AuthResult {
        // TODO: Replace with Ktor + JWT implementation
        delay(1200)
        return AuthResult.Success
    }

    override suspend fun signOut() {
        // TODO: Replace with secure storage implementation to clear tokens
    }
}
