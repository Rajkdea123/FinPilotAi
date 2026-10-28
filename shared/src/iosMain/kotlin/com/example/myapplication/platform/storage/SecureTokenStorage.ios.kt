package com.example.myapplication.platform.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

actual class SecureTokenStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual constructor()

    override suspend fun saveAccessToken(token: String) = withContext(Dispatchers.Default) {
        userDefaults.setObject(token, "access_token")
    }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.Default) {
        userDefaults.stringForKey("access_token")
    }

    override suspend fun saveRefreshToken(token: String) = withContext(Dispatchers.Default) {
        userDefaults.setObject(token, "refresh_token")
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.Default) {
        userDefaults.stringForKey("refresh_token")
    }

    override suspend fun clearTokens() = withContext(Dispatchers.Default) {
        userDefaults.removeObjectForKey("access_token")
        userDefaults.removeObjectForKey("refresh_token")
    }
}
