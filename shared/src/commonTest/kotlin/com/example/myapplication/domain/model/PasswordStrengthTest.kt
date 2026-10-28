package com.example.myapplication.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordStrengthTest {

    @Test
    fun `password shorter than 8 characters returns Weak`() {
        assertEquals(PasswordStrength.Weak, calculatePasswordStrength("pass"))
    }

    @Test
    fun `password with only lowercase returns Weak`() {
        assertEquals(PasswordStrength.Weak, calculatePasswordStrength("password"))
    }

    @Test
    fun `password with lowercase and uppercase returns Fair`() {
        assertEquals(PasswordStrength.Fair, calculatePasswordStrength("Password"))
    }

    @Test
    fun `password with lowercase and digits returns Fair`() {
        assertEquals(PasswordStrength.Fair, calculatePasswordStrength("password123"))
    }

    @Test
    fun `password with lowercase and special chars returns Fair`() {
        assertEquals(PasswordStrength.Fair, calculatePasswordStrength("password!"))
    }

    @Test
    fun `password with lowercase uppercase and digits returns Strong`() {
        assertEquals(PasswordStrength.Strong, calculatePasswordStrength("Password123"))
    }

    @Test
    fun `password with lowercase uppercase and special chars returns Strong`() {
        assertEquals(PasswordStrength.Strong, calculatePasswordStrength("Password!"))
    }

    @Test
    fun `password with lowercase digits and special chars returns Strong`() {
        assertEquals(PasswordStrength.Strong, calculatePasswordStrength("password123!"))
    }

    @Test
    fun `password with all four varieties returns Strong`() {
        assertEquals(PasswordStrength.Strong, calculatePasswordStrength("Password123!"))
    }

    @Test
    fun `empty password returns Weak`() {
        assertEquals(PasswordStrength.Weak, calculatePasswordStrength(""))
    }
}
