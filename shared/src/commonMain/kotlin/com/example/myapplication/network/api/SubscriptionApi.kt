package com.example.myapplication.network.api

import com.example.myapplication.network.dto.CreateSubscriptionRequest
import com.example.myapplication.network.dto.SubscriptionDto
import com.example.myapplication.network.dto.UpdateSubscriptionRequest

interface SubscriptionApi {
    suspend fun getSubscriptions(): List<SubscriptionDto>
    suspend fun createSubscription(request: CreateSubscriptionRequest): SubscriptionDto
    suspend fun updateSubscription(request: UpdateSubscriptionRequest): SubscriptionDto
}
