package com.example.myapplication.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

data class DayGroup(
    val date: LocalDate,
    val label: String,
    val totalPaise: Long,
    val items: List<Expense>
)

fun groupByDay(expenses: List<Expense>): List<DayGroup> {
    if (expenses.isEmpty()) return emptyList()

    val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(timeZone).date
    val yesterday = today.minus(1, kotlinx.datetime.DateTimeUnit.DAY)

    return expenses
        .groupBy { expense ->
            expense.timestamp.toLocalDateTime(timeZone).date
        }
        .map { (date, items) ->
            val label = when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> {
                    val dayOfMonth = date.dayOfMonth
                    val month = date.month.name.take(3)
                    "$month, $dayOfMonth"
                }
            }
            DayGroup(
                date = date,
                label = label,
                totalPaise = items.sumOf { it.amountPaise },
                items = items
            )
        }
        .sortedByDescending { it.date }
}
