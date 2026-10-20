package com.example.myapplication.util

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {
    @Test
    fun `format whole rupees`() {
        assertEquals("₹1,000", MoneyFormatter.formatPaise(100000))
        assertEquals("₹42,500", MoneyFormatter.formatPaise(4250000))
        assertEquals("₹100", MoneyFormatter.formatPaise(10000))
    }

    @Test
    fun `format rupees with paise`() {
        assertEquals("₹1,000.50", MoneyFormatter.formatPaise(100050))
        assertEquals("₹42,500.99", MoneyFormatter.formatPaise(4250099))
        assertEquals("₹100.25", MoneyFormatter.formatPaise(10025))
    }

    @Test
    fun `format zero`() {
        assertEquals("₹0", MoneyFormatter.formatPaise(0))
    }

    @Test
    fun `format small amounts`() {
        assertEquals("₹1", MoneyFormatter.formatPaise(100))
        assertEquals("₹0.50", MoneyFormatter.formatPaise(50))
        assertEquals("₹0.01", MoneyFormatter.formatPaise(1))
    }

    @Test
    fun `format large amounts`() {
        assertEquals("₹1,00,000", MoneyFormatter.formatPaise(10000000))
        assertEquals("₹10,00,000", MoneyFormatter.formatPaise(100000000))
    }
}
