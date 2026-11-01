package com.example.myapplication.platform.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class SecureTokenStorage actual constructor() : TokenStorage {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun ensureInitialized() {
        if (!::sharedPreferences.isInitialized) {
            throw IllegalStateException("SecureTokenStorage not initialized. Call init(context) first.")
        }
    }

    actual override suspend fun saveAccessToken(token: String): Unit = withContext(Dispatchers.IO) {
        ensureInitialized()
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    actual override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        ensureInitialized()
        sharedPreferences.getString("access_token", null)
    }

    actual override suspend fun saveRefreshToken(token: String): Unit = withContext(Dispatchers.IO) {
        ensureInitialized()
        sharedPreferences.edit().putString("refresh_token", token).apply()
    }

    actual override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        ensureInitialized()
        sharedPreferences.getString("refresh_token", null)
    }

    actual override suspend fun clearTokens(): Unit = withContext(Dispatchers.IO) {
        ensureInitialized()
        sharedPreferences.edit().clear().apply()
    }
}
