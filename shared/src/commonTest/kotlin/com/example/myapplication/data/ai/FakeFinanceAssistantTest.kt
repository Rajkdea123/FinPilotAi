package com.example.myapplication.data.ai

import com.example.myapplication.domain.model.ChatMessage
import com.example.myapplication.domain.model.MessageRole
import com.example.myapplication.domain.model.MessageStatus
import com.example.myapplication.domain.model.SpendInsight
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FakeFinanceAssistantTest {
    private val assistant = FakeFinanceAssistant()

    @Test
    fun `food keyword returns food spending insight`() = runTest {
        val result = assistant.ask("How much did I spend on food?", emptyList())
        val message = result.getOrNull()
        assertNotNull(message)
        assertEquals(MessageRole.Assistant, message.role)
        assertEquals("You spent ₹8,420 on food last month, 18% more than the month before.", message.text)
        assertNotNull(message.insight)
        assertEquals("Food", message.insight?.label)
        assertEquals(842000L, message.insight?.valuePaise)
        assertEquals(0.8f, message.insight?.ratio)
        assertEquals(46, message.insight?.basedOnCount)
    }

    @Test
    fun `too much keyword returns spending rise response`() = runTest {
        val result = assistant.ask("Why am I spending too much?", emptyList())
        val message = result.getOrNull()
        assertNotNull(message)
        assertEquals(MessageRole.Assistant, message.role)
        assertEquals("Food and online shopping rose 23% and 31%. Want a weekly cap for both?", message.text)
        assertNull(message.insight)
    }

    @Test
    fun `unknown keyword returns fallback response`() = runTest {
        val result = assistant.ask("What's the weather?", emptyList())
        val message = result.getOrNull()
        assertNotNull(message)
        assertEquals(MessageRole.Assistant, message.role)
        assertTrue(message.text.contains("help you understand your spending"))
        assertNull(message.insight)
    }

    @Test
    fun `case insensitive keyword matching`() = runTest {
        val result = assistant.ask("FOOD spending", emptyList())
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message.text.contains("₹8,420"))
    }

    @Test
    fun `partial keyword matching`() = runTest {
        val result = assistant.ask("I spent too much on food", emptyList())
        val message = result.getOrNull()
        assertNotNull(message)
        assertTrue(message.text.contains("₹8,420"))
    }
}
