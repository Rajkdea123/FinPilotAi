package com.example.myapplication.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatePasswordUseCaseTest {
    private val validatePasswordUseCase = ValidatePasswordUseCase()

    @Test
    fun `valid password returns success`() {
        val result = validatePasswordUseCase("password123")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `blank password returns error`() {
        val result = validatePasswordUseCase("")
        assertFalse(result.isValid)
        assertEquals("Password is required", result.errorMessage)
    }

    @Test
    fun `password shorter than 8 characters returns error`() {
        val result = validatePasswordUseCase("pass")
        assertFalse(result.isValid)
        assertEquals("Password must be at least 8 characters", result.errorMessage)
    }

    @Test
    fun `password exactly 8 characters returns success`() {
        val result = validatePasswordUseCase("12345678")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }
}
