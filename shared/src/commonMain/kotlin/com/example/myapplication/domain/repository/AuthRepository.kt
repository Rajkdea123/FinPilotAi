package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AuthResult

interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(name: String, email: String, password: String): AuthResult
    suspend fun requestPasswordReset(email: String): AuthResult
    suspend fun signOut()
}
