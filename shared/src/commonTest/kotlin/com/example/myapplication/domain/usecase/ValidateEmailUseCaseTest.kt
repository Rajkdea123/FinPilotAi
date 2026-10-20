package com.example.myapplication.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateEmailUseCaseTest {
    private val validateEmailUseCase = ValidateEmailUseCase()

    @Test
    fun `valid email returns success`() {
        val result = validateEmailUseCase("test@example.com")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `blank email returns error`() {
        val result = validateEmailUseCase("")
        assertFalse(result.isValid)
        assertEquals("Email is required", result.errorMessage)
    }

    @Test
    fun `invalid email format returns error`() {
        val result = validateEmailUseCase("invalid-email")
        assertFalse(result.isValid)
        assertEquals("Invalid email format", result.errorMessage)
    }

    @Test
    fun `email without domain returns error`() {
        val result = validateEmailUseCase("test@")
        assertFalse(result.isValid)
        assertEquals("Invalid email format", result.errorMessage)
    }

    @Test
    fun `email without @ returns error`() {
        val result = validateEmailUseCase("testexample.com")
        assertFalse(result.isValid)
        assertEquals("Invalid email format", result.errorMessage)
    }
}
