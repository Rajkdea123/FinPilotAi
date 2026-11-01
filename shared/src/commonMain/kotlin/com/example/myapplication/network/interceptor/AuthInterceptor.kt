package com.example.myapplication.network.interceptor

import com.example.myapplication.platform.storage.TokenStorage
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer

fun HttpClientConfig<*>.configureAuthInterceptor(tokenStorage: TokenStorage) {
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
                null
            }
        }
    }
}
