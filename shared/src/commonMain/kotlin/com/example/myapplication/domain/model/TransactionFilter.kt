package com.example.myapplication.domain.model

import kotlinx.datetime.LocalDate

data class TransactionFilter(
    val query: String = "",
    val categories: Set<Category> = emptySet(),
    val paymentMethods: Set<String> = emptySet(),
    val dateRange: DateRange? = null
)

data class DateRange(
    val start: LocalDate,
    val end: LocalDate
)

enum class SortOrder {
    NewestFirst,
    OldestFirst,
    HighestAmount
}
