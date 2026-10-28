package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.DayGroup
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.SortOrder
import com.example.myapplication.domain.model.TransactionFilter
import com.example.myapplication.domain.model.groupByDay
import com.example.myapplication.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTransactionsUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(filter: TransactionFilter, sort: SortOrder): Flow<List<DayGroup>> {
        return expenseRepository.observeAll(filter, sort).map { expenses ->
            groupByDay(expenses)
        }
    }
}
