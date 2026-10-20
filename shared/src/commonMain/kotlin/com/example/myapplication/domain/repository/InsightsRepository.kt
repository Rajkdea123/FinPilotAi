package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Insight
import com.example.myapplication.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface InsightsRepository {
    fun observeInsights(): Flow<List<Insight>>
    suspend fun dismiss(id: String)
    suspend fun restore(id: String)
    fun observeSubscriptions(): Flow<List<Subscription>>
    suspend fun markCancelled(id: String)
}
