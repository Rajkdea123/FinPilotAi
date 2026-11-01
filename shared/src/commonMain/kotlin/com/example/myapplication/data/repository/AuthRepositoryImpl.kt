package com.example.myapplication.data.repository

import com.example.myapplication.di.core.AppCoroutineDispatchers
import com.example.myapplication.domain.model.AuthResult
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.network.api.AuthApi
import com.example.myapplication.network.dto.LoginRequest
import com.example.myapplication.network.dto.RefreshTokenRequest
import com.example.myapplication.network.dto.SignupRequest
import com.example.myapplication.platform.storage.TokenStorage
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val dispatchers: AppCoroutineDispatchers
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): AuthResult = withContext(dispatchers.io) {
        try {
            val response = authApi.login(LoginRequest(email, password))
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): AuthResult = withContext(dispatchers.io) {
        try {
            val response = authApi.signup(SignupRequest(email, password, name))
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Signup failed")
        }
    }

    override suspend fun requestPasswordReset(email: String): AuthResult = withContext(dispatchers.io) {
        try {
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Password reset failed")
        }
    }

    override suspend fun signOut(): Unit = withContext(dispatchers.io) {
        try {
            authApi.logout()
        } catch (e: Exception) {
            // Ignore logout errors, just clear local tokens
        } finally {
            tokenStorage.clearTokens()
        }
    }

    suspend fun refreshToken(): Boolean = withContext(dispatchers.io) {
        try {
            val refreshToken = tokenStorage.getRefreshToken()
            if (refreshToken == null) {
                return@withContext false
            }
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            true
        } catch (e: Exception) {
            tokenStorage.clearTokens()
            false
        }
    }

    suspend fun isLoggedIn(): Boolean = withContext(dispatchers.io) {
        tokenStorage.getAccessToken() != null
    }
}
