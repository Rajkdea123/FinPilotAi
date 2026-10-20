package com.example.myapplication.data.ai

import com.example.myapplication.domain.ai.ExpenseParser
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.ParsedExpense
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class FakeExpenseParser : ExpenseParser {
    private val merchantKeywords = mapOf(
        "zomato" to Pair(Category.Food, "Zomato"),
        "swiggy" to Pair(Category.Food, "Swiggy"),
        "uber" to Pair(Category.Transport, "Uber"),
        "metro" to Pair(Category.Transport, "Metro"),
        "amazon" to Pair(Category.Shopping, "Amazon"),
        "flipkart" to Pair(Category.Shopping, "Flipkart"),
        "netflix" to Pair(Category.Entertainment, "Netflix"),
        "spotify" to Pair(Category.Entertainment, "Spotify"),
        "rent" to Pair(Category.Bills, "Rent"),
        "electricity" to Pair(Category.Bills, "Electricity")
    )

    override suspend fun parse(text: String): Result<ParsedExpense> {
        // TODO: Replace with Ktor + LLM implementation
        delay(600)

        return try {
            val amountPaise = extractAmount(text)
            val (category, merchant) = extractMerchantAndCategory(text)
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            Result.success(
                ParsedExpense(
                    amountPaise = amountPaise,
                    category = category,
                    merchant = merchant,
                    date = date
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractAmount(text: String): Long? {
        // Look for ₹ or Rs followed by a number
        val rupeePattern = Regex("[₹Rs]\\s*([\\d,]+(?:\\.\\d+)?)")
        val match = rupeePattern.find(text) ?: return null
        
        val amountStr = match.groupValues[1].replace(",", "")
        val amount = amountStr.toDoubleOrNull() ?: return null
        
        return (amount * 100).toLong()
    }

    private fun extractMerchantAndCategory(text: String): Pair<Category?, String?> {
        val lowerText = text.lowercase()
        
        for ((keyword, pair) in merchantKeywords) {
            if (keyword in lowerText) {
                return pair
            }
        }
        
        return null to null
    }
}
