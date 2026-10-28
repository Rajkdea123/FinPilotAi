package com.example.myapplication.network.interceptor

import com.example.myapplication.platform.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth/providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.header

fun HttpClient.configureAuthInterceptor(tokenStorage: TokenStorage) {
    install(Auth) {
        bearer {
            loadTokens {
                val accessToken = tokenStorage.getAccessToken()
                if (accessToken != null) {
                    BearerTokens(accessToken, tokenStorage.getRefreshToken() ?: "")
                } else {
                    null
                }
            }
            refreshTokens {
                // Token refresh logic would go here
                // For now, return null to force re-login
                null
            }
        }
    }
}
