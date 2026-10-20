package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Category
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateBudgetUseCaseTest {
    private val validateBudgetUseCase = ValidateBudgetUseCase()

    @Test
    fun `valid budget returns success`() {
        val limits = mapOf(
            Category.Food to 10000_00L,
            Category.Shopping to 5000_00L
        )
        val result = validateBudgetUseCase(20000_00L, limits)
        assertTrue(result.isValid)
        assertEquals(null, result.totalError)
        assertEquals(null, result.categorySumError)
    }

    @Test
    fun `zero total returns total error`() {
        val limits = mapOf(Category.Food to 5000_00L)
        val result = validateBudgetUseCase(0L, limits)
        assertFalse(result.isValid)
        assertEquals("Total budget must be greater than 0", result.totalError)
    }

    @Test
    fun `category limits sum exceeding total returns sum error`() {
        val limits = mapOf(
            Category.Food to 15000_00L,
            Category.Shopping to 10000_00L
        )
        val result = validateBudgetUseCase(20000_00L, limits)
        assertFalse(result.isValid)
        assertEquals("Sum of category limits cannot exceed total budget", result.categorySumError)
    }
}
