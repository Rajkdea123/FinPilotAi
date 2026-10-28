package com.example.myapplication.network

import io.ktor.client.HttpClient

expect class HttpClientFactory {
    fun createClient(): HttpClient
}
