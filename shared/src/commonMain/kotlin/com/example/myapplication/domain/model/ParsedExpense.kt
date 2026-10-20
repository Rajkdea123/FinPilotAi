package com.example.myapplication.domain.model

import kotlinx.datetime.LocalDate

data class ParsedExpense(
    val amountPaise: Long?,
    val category: Category?,
    val merchant: String?,
    val date: LocalDate?
)
