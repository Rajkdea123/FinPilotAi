package com.example.myapplication.network.client

import com.example.myapplication.network.api.ExpenseApi
import com.example.myapplication.network.dto.CreateExpenseRequest
import com.example.myapplication.network.dto.DeleteExpenseRequest
import com.example.myapplication.network.dto.ExpenseDto
import com.example.myapplication.network.dto.UpdateExpenseRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ExpenseApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : ExpenseApi {

    override suspend fun getExpenses(): List<ExpenseDto> {
        return httpClient.get("$baseUrl/expenses").body()
    }

    override suspend fun getExpenseById(id: String): ExpenseDto {
        return httpClient.get("$baseUrl/expenses/$id").body()
    }

    override suspend fun createExpense(request: CreateExpenseRequest): ExpenseDto {
        return httpClient.post("$baseUrl/expenses") {
            setBody(request)
        }.body()
    }

    override suspend fun updateExpense(request: UpdateExpenseRequest): ExpenseDto {
        return httpClient.put("$baseUrl/expenses/${request.id}") {
            setBody(request)
        }.body()
    }

    override suspend fun deleteExpense(request: DeleteExpenseRequest) {
        httpClient.delete("$baseUrl/expenses/${request.id}") {
            setBody(request)
        }
    }
}
