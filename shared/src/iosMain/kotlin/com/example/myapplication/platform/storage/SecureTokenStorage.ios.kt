package com.example.myapplication.platform.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

actual class SecureTokenStorage actual constructor() : TokenStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual override suspend fun saveAccessToken(token: String): Unit = withContext(Dispatchers.Default) {
        userDefaults.setObject(token, "access_token")
    }

    actual override suspend fun getAccessToken(): String? = withContext(Dispatchers.Default) {
        userDefaults.stringForKey("access_token")
    }

    actual override suspend fun saveRefreshToken(token: String): Unit = withContext(Dispatchers.Default) {
        userDefaults.setObject(token, "refresh_token")
    }

    actual override suspend fun getRefreshToken(): String? = withContext(Dispatchers.Default) {
        userDefaults.stringForKey("refresh_token")
    }

    actual override suspend fun clearTokens(): Unit = withContext(Dispatchers.Default) {
        userDefaults.removeObjectForKey("access_token")
        userDefaults.removeObjectForKey("refresh_token")
    }
}
