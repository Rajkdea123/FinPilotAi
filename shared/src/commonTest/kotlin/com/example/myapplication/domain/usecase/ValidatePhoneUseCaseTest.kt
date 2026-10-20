package com.example.myapplication.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatePhoneUseCaseTest {
    private val validatePhoneUseCase = ValidatePhoneUseCase()

    @Test
    fun `empty phone is valid`() {
        val result = validatePhoneUseCase("")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `valid 10 digit phone returns success`() {
        val result = validatePhoneUseCase("9876543210")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `valid phone with plus 91 prefix returns success`() {
        val result = validatePhoneUseCase("+91 9876543210")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `invalid phone with wrong length returns error`() {
        val result = validatePhoneUseCase("12345")
        assertFalse(result.isValid)
        assertEquals("Phone number must be 10 digits", result.errorMessage)
    }

    @Test
    fun `invalid phone with non digits returns error`() {
        val result = validatePhoneUseCase("98765abcde")
        assertFalse(result.isValid)
        assertEquals("Phone number must be 10 digits", result.errorMessage)
    }
}
