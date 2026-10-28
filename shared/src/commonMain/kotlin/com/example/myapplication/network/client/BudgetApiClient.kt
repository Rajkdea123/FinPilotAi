package com.example.myapplication.network.client

import com.example.myapplication.network.api.BudgetApi
import com.example.myapplication.network.dto.BudgetDto
import com.example.myapplication.network.dto.CreateBudgetRequest
import com.example.myapplication.network.dto.UpdateBudgetRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class BudgetApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : BudgetApi {

    override suspend fun getBudgets(): List<BudgetDto> {
        return httpClient.get("$baseUrl/budgets").body()
    }

    override suspend fun createBudget(request: CreateBudgetRequest): BudgetDto {
        return httpClient.post("$baseUrl/budgets") {
            setBody(request)
        }.body()
    }

    override suspend fun updateBudget(request: UpdateBudgetRequest): BudgetDto {
        return httpClient.put("$baseUrl/budgets/${request.id}") {
            setBody(request)
        }.body()
    }
}
