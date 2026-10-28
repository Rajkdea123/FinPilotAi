package com.example.myapplication.network.client

import com.example.myapplication.network.api.SubscriptionApi
import com.example.myapplication.network.dto.CreateSubscriptionRequest
import com.example.myapplication.network.dto.SubscriptionDto
import com.example.myapplication.network.dto.UpdateSubscriptionRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class SubscriptionApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : SubscriptionApi {

    override suspend fun getSubscriptions(): List<SubscriptionDto> {
        return httpClient.get("$baseUrl/subscriptions").body()
    }

    override suspend fun createSubscription(request: CreateSubscriptionRequest): SubscriptionDto {
        return httpClient.post("$baseUrl/subscriptions") {
            setBody(request)
        }.body()
    }

    override suspend fun updateSubscription(request: UpdateSubscriptionRequest): SubscriptionDto {
        return httpClient.put("$baseUrl/subscriptions/${request.id}") {
            setBody(request)
        }.body()
    }
}
