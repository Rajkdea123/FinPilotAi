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

    override suspend fun signOut() {
        // TODO: Replace with secure storage implementation to clear tokens
    }
}
