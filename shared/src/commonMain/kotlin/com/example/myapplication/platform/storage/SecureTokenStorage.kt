package com.example.myapplication.platform.storage

expect class SecureTokenStorage() : TokenStorage {
    override suspend fun saveAccessToken(token: String)
    override suspend fun getAccessToken(): String?
    override suspend fun saveRefreshToken(token: String)
    override suspend fun getRefreshToken(): String?
    override suspend fun clearTokens()
}
