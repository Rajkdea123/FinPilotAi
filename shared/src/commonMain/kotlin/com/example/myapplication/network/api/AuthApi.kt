package com.example.myapplication.network.api

import com.example.myapplication.network.dto.AuthResponse
import com.example.myapplication.network.dto.LoginRequest
import com.example.myapplication.network.dto.RefreshTokenRequest
import com.example.myapplication.network.dto.RefreshTokenResponse
import com.example.myapplication.network.dto.SignupRequest

interface AuthApi {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun signup(request: SignupRequest): AuthResponse
    suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse
    suspend fun logout()
}
