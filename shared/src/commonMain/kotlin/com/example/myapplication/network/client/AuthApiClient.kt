package com.example.myapplication.network.client

import com.example.myapplication.network.api.AuthApi
import com.example.myapplication.network.dto.AuthResponse
import com.example.myapplication.network.dto.LoginRequest
import com.example.myapplication.network.dto.RefreshTokenRequest
import com.example.myapplication.network.dto.RefreshTokenResponse
import com.example.myapplication.network.dto.SignupRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : AuthApi {

    override suspend fun login(request: LoginRequest): AuthResponse {
        return httpClient.post("$baseUrl/auth/login") {
            setBody(request)
        }.body()
    }

    override suspend fun signup(request: SignupRequest): AuthResponse {
        return httpClient.post("$baseUrl/auth/signup") {
            setBody(request)
        }.body()
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse {
        return httpClient.post("$baseUrl/auth/refresh") {
            setBody(request)
        }.body()
    }

    override suspend fun logout() {
        httpClient.post("$baseUrl/auth/logout")
    }
}
