package com.example.myapplication.domain.model

import kotlinx.datetime.Instant

enum class Category {
    Food,
    Shopping,
    Transport,
    Bills,
    Entertainment
}

data class Expense(
    val id: String,
    val amountPaise: Long,
    val category: Category,
    val merchant: String,
    val paymentMethod: String,
    val timestamp: Instant
)
