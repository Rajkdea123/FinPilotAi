package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AuthResult

interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signOut()
}
