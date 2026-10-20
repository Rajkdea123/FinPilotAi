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
        assertEquals("Name cannot be blank", result.errorMessage)
    }
}
