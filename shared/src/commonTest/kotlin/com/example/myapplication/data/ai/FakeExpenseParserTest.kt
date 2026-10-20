package com.example.myapplication.data.ai

import com.example.myapplication.domain.model.Category
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FakeExpenseParserTest {
    private val parser = FakeExpenseParser()

    @Test
    fun `parse amount with ₹ symbol`() = runTest {
        val result = parser.parse("Paid ₹450 at Zomato")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(45000L, parsed.amountPaise)
    }

    @Test
    fun `parse amount with Rs symbol`() = runTest {
        val result = parser.parse("Rs 300 for Metro")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(30000L, parsed.amountPaise)
    }

    @Test
    fun `parse amount with commas`() = runTest {
        val result = parser.parse("Spent ₹1,000 on Amazon")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(100000L, parsed.amountPaise)
    }

    @Test
    fun `parse amount with decimal`() = runTest {
        val result = parser.parse("Paid ₹450.50 at Swiggy")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(45050L, parsed.amountPaise)
    }

    @Test
    fun `parse Zomato merchant as Food category`() = runTest {
        val result = parser.parse("Paid ₹450 at Zomato")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(Category.Food, parsed.category)
        assertEquals("Zomato", parsed.merchant)
    }

    @Test
    fun `parse Swiggy merchant as Food category`() = runTest {
        val result = parser.parse("Swiggy order ₹300")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(Category.Food, parsed.category)
        assertEquals("Swiggy", parsed.merchant)
    }

    @Test
    fun `parse Uber merchant as Transport category`() = runTest {
        val result = parser.parse("Uber ride ₹500")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(Category.Transport, parsed.category)
        assertEquals("Uber", parsed.merchant)
    }

    @Test
    fun `parse Metro merchant as Transport category`() = runTest {
        val result = parser.parse("Metro card ₹200")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(Category.Transport, parsed.category)
        assertEquals("Metro", parsed.merchant)
    }

    @Test
    fun `parse Amazon merchant as Shopping category`() = runTest {
        val result = parser.parse("Amazon purchase ₹1,000")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(Category.Shopping, parsed.category)
        assertEquals("Amazon", parsed.merchant)
    }

    @Test
    fun `parse Rent merchant as Bills category`() = runTest {
        val result = parser.parse("Rent ₹20,000")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(Category.Bills, parsed.category)
        assertEquals("Rent", parsed.merchant)
    }

    @Test
    fun `parse Netflix merchant as Entertainment category`() = runTest {
        val result = parser.parse("Netflix ₹500")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertEquals(Category.Entertainment, parsed.category)
        assertEquals("Netflix", parsed.merchant)
    }

    @Test
    fun `parse returns null category for unknown merchant`() = runTest {
        val result = parser.parse("Paid ₹450 at UnknownStore")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertNull(parsed.category)
        assertNull(parsed.merchant)
    }

    @Test
    fun `parse returns null amount when no amount found`() = runTest {
        val result = parser.parse("Bought something at Zomato")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertNull(parsed.amountPaise)
    }

    @Test
    fun `parse returns today's date by default`() = runTest {
        val result = parser.parse("Paid ₹450 at Zomato")
        val parsed = result.getOrNull()
        assertNotNull(parsed)
        assertNotNull(parsed.date)
    }
}
