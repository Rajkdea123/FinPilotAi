package com.example.myapplication.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateConfirmPasswordUseCaseTest {
    private val validateConfirmPasswordUseCase = ValidateConfirmPasswordUseCase()

    @Test
    fun `matching passwords returns success`() {
        val result = validateConfirmPasswordUseCase("password123", "password123")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `blank confirm password returns error`() {
        val result = validateConfirmPasswordUseCase("password123", "")
        assertFalse(result.isValid)
        assertEquals("Please confirm your password", result.errorMessage)
    }

    @Test
    fun `mismatched passwords returns error`() {
        val result = validateConfirmPasswordUseCase("password123", "password456")
        assertFalse(result.isValid)
        assertEquals("Passwords do not match", result.errorMessage)
    }

    @Test
    fun `case sensitive password mismatch returns error`() {
        val result = validateConfirmPasswordUseCase("Password123", "password123")
        assertFalse(result.isValid)
        assertEquals("Passwords do not match", result.errorMessage)
    }
}
