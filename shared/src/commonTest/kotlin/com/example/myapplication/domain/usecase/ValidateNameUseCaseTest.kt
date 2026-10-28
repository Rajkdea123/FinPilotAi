package com.example.myapplication.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateNameUseCaseTest {
    private val validateNameUseCase = ValidateNameUseCase()

    @Test
    fun `valid name returns success`() {
        val result = validateNameUseCase("John Doe")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `blank name returns error`() {
        val result = validateNameUseCase("   ")
        assertFalse(result.isValid)
        assertEquals("Name is required", result.errorMessage)
    }

    @Test
    fun `empty name returns error`() {
        val result = validateNameUseCase("")
        assertFalse(result.isValid)
        assertEquals("Name is required", result.errorMessage)
    }

    @Test
    fun `name shorter than 2 characters returns error`() {
        val result = validateNameUseCase("J")
        assertFalse(result.isValid)
        assertEquals("Name must be at least 2 characters", result.errorMessage)
    }

    @Test
    fun `name exactly 2 characters returns success`() {
        val result = validateNameUseCase("Jo")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }
}
