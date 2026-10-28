package com.example.myapplication.di.network

import com.example.myapplication.network.HttpClientFactory
import com.example.myapplication.network.api.AuthApi
import com.example.myapplication.network.api.BudgetApi
import com.example.myapplication.network.api.ChatApi
import com.example.myapplication.network.api.ExpenseApi
import com.example.myapplication.network.api.SubscriptionApi
import com.example.myapplication.network.client.AuthApiClient
import com.example.myapplication.network.client.BudgetApiClient
import com.example.myapplication.network.client.ChatApiClient
import com.example.myapplication.network.client.ExpenseApiClient
import com.example.myapplication.network.client.SubscriptionApiClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClientFactory> { HttpClientFactory() }
    single<HttpClient> { get<HttpClientFactory>().createClient() }
    
    single<AuthApi> { AuthApiClient(get(), "https://api.finpilot.example.com") }
    single<ExpenseApi> { ExpenseApiClient(get(), "https://api.finpilot.example.com") }
    single<BudgetApi> { BudgetApiClient(get(), "https://api.finpilot.example.com") }
    single<SubscriptionApi> { SubscriptionApiClient(get(), "https://api.finpilot.example.com") }
    single<ChatApi> { ChatApiClient(get(), "https://api.finpilot.example.com") }
}
