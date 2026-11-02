package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Expense
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRecentExpensesUseCaseTest {

    @Test
    fun `returns at most 5 items by default when more than 5 exist`() = runTest {
        val fakeRepo = FakeExpenseRepository()
        repeat(8) { index ->
            fakeRepo.addExpense(
                Expense(
                    id = "exp-$index",
                    amountPaise = (1000 + index * 100).toLong(),
                    category = Category.Food,
                    merchant = "Merchant $index",
                    paymentMethod = "Card",
                    timestamp = Instant.fromEpochMilliseconds(1000000L + index * 1000L)
                )
            )
        }

        val useCase = GetRecentExpensesUseCase(fakeRepo)
        val result = useCase().first()

        assertTrue(result.size <= 5)
        assertEquals(5, result.size)
    }

    @Test
    fun `returns items ordered newest first`() = runTest {
        val fakeRepo = FakeExpenseRepository()
        fakeRepo.addExpense(Expense("1", 100, Category.Food, "Older", "Card", Instant.fromEpochMilliseconds(1000L)))
        fakeRepo.addExpense(Expense("2", 200, Category.Food, "Newer", "Card", Instant.fromEpochMilliseconds(2000L)))

        val useCase = GetRecentExpensesUseCase(fakeRepo)
        val result = useCase().first()

        assertEquals("Newer", result.first().merchant)
    }
}
