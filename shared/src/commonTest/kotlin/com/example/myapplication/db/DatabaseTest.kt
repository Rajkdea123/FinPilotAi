package com.example.myapplication.db

import app.cash.sqldelight.driver.sqlite.SqliteDriver
import com.example.myapplication.di.AppCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseTest {

    @Test
    fun testDatabaseSchema() = runTest {
        val driver = SqliteDriver(FinPilotDatabase.Schema, ":memory:")
        val database = FinPilotDatabase(driver)
        val clock = Clock.System
        val dispatchers = AppCoroutineDispatchers(
            main = kotlinx.coroutines.Dispatchers.Main.immediate,
            io = kotlinx.coroutines.Dispatchers.Default,
            default = kotlinx.coroutines.Dispatchers.Default
        )

        // Test expense insertion
        database.expenseQueries.insertExpense(
            id = "test-expense-1",
            amount = 1000L,
            category = "Food",
            paymentMethod = "UPI",
            date = clock.now().toEpochMilliseconds(),
            description = "Test expense",
            version = 1,
            updatedAt = clock.now().toEpochMilliseconds(),
            deviceId = "device-1",
            isDeleted = 0,
            syncState = "SYNCED"
        )

        val expenses = database.expenseQueries.selectAllExpenses().executeAsList()
        assertEquals(1, expenses.size)
        assertEquals("test-expense-1", expenses.first().id)
        assertEquals("Food", expenses.first().category)

        // Test budget insertion
        database.budgetQueries.insertBudget(
            id = "test-budget-1",
            total = 50000L,
            category = "TOTAL",
            limit = 50000L,
            version = 1,
            updatedAt = clock.now().toEpochMilliseconds(),
            deviceId = "device-1",
            isDeleted = 0,
            syncState = "SYNCED"
        )

        val budgets = database.budgetQueries.selectAllBudgets().executeAsList()
        assertEquals(1, budgets.size)
        assertEquals(50000L, budgets.first().total)

        // Test subscription insertion
        database.subscriptionQueries.insertSubscription(
            id = "test-sub-1",
            name = "Netflix",
            amount = 500L,
            category = "Entertainment",
            billingCycle = "MONTHLY",
            nextBillingDate = clock.now().toEpochMilliseconds(),
            isActive = 1,
            version = 1,
            updatedAt = clock.now().toEpochMilliseconds(),
            deviceId = "device-1",
            isDeleted = 0,
            syncState = "SYNCED"
        )

        val subscriptions = database.subscriptionQueries.selectAllSubscriptions().executeAsList()
        assertEquals(1, subscriptions.size)
        assertEquals("Netflix", subscriptions.first().name)

        // Test insight insertion
        database.insightQueries.insertInsight(
            id = "test-insight-1",
            title = "Test Insight",
            content = "Test content",
            category = "SpendingAlert",
            date = clock.now().toEpochMilliseconds(),
            version = 1,
            updatedAt = clock.now().toEpochMilliseconds(),
            deviceId = "device-1",
            isDeleted = 0,
            syncState = "SYNCED"
        )

        val insights = database.insightQueries.selectAllInsights().executeAsList()
        assertEquals(1, insights.size)
        assertEquals("Test Insight", insights.first().title)

        // Test chat message insertion
        database.chatMessageQueries.insertChatMessage(
            id = "test-chat-1",
            role = "ASSISTANT",
            content = "Hello",
            timestamp = clock.now().toEpochMilliseconds(),
            version = 1,
            updatedAt = clock.now().toEpochMilliseconds(),
            deviceId = "device-1",
            isDeleted = 0,
            syncState = "SYNCED"
        )

        val chatMessages = database.chatMessageQueries.selectAllChatMessages().executeAsList()
        assertEquals(1, chatMessages.size)
        assertEquals("ASSISTANT", chatMessages.first().role)

        // Test user profile insertion
        database.userProfileQueries.insertUserProfile(
            id = "current",
            name = "Test User",
            email = "test@example.com",
            avatarUrl = null,
            version = 1,
            updatedAt = clock.now().toEpochMilliseconds(),
            deviceId = "device-1",
            isDeleted = 0,
            syncState = "SYNCED"
        )

        val profile = database.userProfileQueries.selectUserProfile().executeAsOneOrNull()
        assertNotNull(profile)
        assertEquals("Test User", profile.name)

        // Test sync outbox insertion
        database.syncOutboxQueries.insertSyncOutbox(
            id = "test-outbox-1",
            tableName = "Expense",
            recordId = "test-expense-1",
            operation = "CREATE",
            payload = """{"id":"test-expense-1"}""",
            createdAt = clock.now().toEpochMilliseconds(),
            isProcessed = 0,
            processedAt = null
        )

        val outboxEntries = database.syncOutboxQueries.selectPendingSyncOutbox().executeAsList()
        assertEquals(1, outboxEntries.size)
        assertEquals("Expense", outboxEntries.first().tableName)

        driver.close()
    }

    @Test
    fun testExpenseQueries() = runTest {
        val driver = SqliteDriver(FinPilotDatabase.Schema, ":memory:")
        val database = FinPilotDatabase(driver)
        val clock = Clock.System

        // Insert multiple expenses
        repeat(5) { index ->
            database.expenseQueries.insertExpense(
                id = "expense-$index",
                amount = (index + 1) * 1000L,
                category = listOf("Food", "Shopping", "Transport")[index % 3],
                paymentMethod = "UPI",
                date = clock.now().toEpochMilliseconds() - (index * 86400000L),
                description = "Expense $index",
                version = 1,
                updatedAt = clock.now().toEpochMilliseconds(),
                deviceId = "device-1",
                isDeleted = 0,
                syncState = "SYNCED"
            )
        }

        // Test select by category
        val foodExpenses = database.expenseQueries.selectExpensesByCategory("Food").executeAsList()
        assertEquals(2, foodExpenses.size)

        // Test soft delete
        database.expenseQueries.deleteExpense(
            updatedAt = clock.now().toEpochMilliseconds(),
            id = "expense-0"
        )

        val allExpenses = database.expenseQueries.selectAllExpenses().executeAsList()
        assertEquals(4, allExpenses.size) // One should be soft deleted

        driver.close()
    }
}
