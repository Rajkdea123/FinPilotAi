package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeExpenseRepository
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Expense
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeleteExpenseUseCaseTest {

    @Test
    fun `delete removes expense from repository`() = runTest {
        val repository = FakeExpenseRepository()
        val deleteUseCase = DeleteExpenseUseCase(repository)
        
        // Add a test expense
        val expense = Expense(
            id = "test-1",
            amountPaise = 10000,
            category = Category.Food,
            merchant = "Test Merchant",
            paymentMethod = "UPI",
            timestamp = Clock.System.now()
        )
        repository.addExpense(expense)
        
        // Verify it exists
        val beforeDelete = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val beforeResult = kotlinx.coroutines.flow.first(beforeDelete)
        assertTrue(beforeResult.any { it.id == "test-1" })
        
        // Delete the expense
        deleteUseCase("test-1")
        
        // Verify it's removed
        val afterDelete = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val afterResult = kotlinx.coroutines.flow.first(afterDelete)
        assertTrue(afterResult.none { it.id == "test-1" })
    }

    @Test
    fun `delete with non-existent id does nothing`() = runTest {
        val repository = FakeExpenseRepository()
        val deleteUseCase = DeleteExpenseUseCase(repository)
        
        // Get initial count
        val beforeDelete = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val beforeResult = kotlinx.coroutines.flow.first(beforeDelete)
        val initialCount = beforeResult.size
        
        // Try to delete non-existent expense
        deleteUseCase("non-existent-id")
        
        // Verify count hasn't changed
        val afterDelete = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val afterResult = kotlinx.coroutines.flow.first(afterDelete)
        assertEquals(initialCount, afterResult.size)
    }

    @Test
    fun `delete stores expense in deleted expenses for potential restore`() = runTest {
        val repository = FakeExpenseRepository()
        val deleteUseCase = DeleteExpenseUseCase(repository)
        val restoreUseCase = RestoreExpenseUseCase(repository)
        
        // Add a test expense
        val expense = Expense(
            id = "test-restore-1",
            amountPaise = 10000,
            category = Category.Food,
            merchant = "Test Merchant",
            paymentMethod = "UPI",
            timestamp = Clock.System.now()
        )
        repository.addExpense(expense)
        
        // Delete the expense
        deleteUseCase("test-restore-1")
        
        // Restore the expense
        restoreUseCase(expense)
        
        // Verify it's restored
        val afterRestore = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val afterResult = kotlinx.coroutines.flow.first(afterRestore)
        assertTrue(afterResult.any { it.id == "test-restore-1" })
    }

    @Test
    fun `delete multiple expenses works correctly`() = runTest {
        val repository = FakeExpenseRepository()
        val deleteUseCase = DeleteExpenseUseCase(repository)
        
        // Add test expenses
        val expense1 = Expense(
            id = "test-multi-1",
            amountPaise = 10000,
            category = Category.Food,
            merchant = "Test Merchant 1",
            paymentMethod = "UPI",
            timestamp = Clock.System.now()
        )
        val expense2 = Expense(
            id = "test-multi-2",
            amountPaise = 20000,
            category = Category.Transport,
            merchant = "Test Merchant 2",
            paymentMethod = "Card",
            timestamp = Clock.System.now()
        )
        repository.addExpense(expense1)
        repository.addExpense(expense2)
        
        // Delete both
        deleteUseCase("test-multi-1")
        deleteUseCase("test-multi-2")
        
        // Verify both are removed
        val afterDelete = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val afterResult = kotlinx.coroutines.flow.first(afterDelete)
        assertTrue(afterResult.none { it.id == "test-multi-1" })
        assertTrue(afterResult.none { it.id == "test-multi-2" })
    }

    @Test
    fun `delete and restore cycle works correctly`() = runTest {
        val repository = FakeExpenseRepository()
        val deleteUseCase = DeleteExpenseUseCase(repository)
        val restoreUseCase = RestoreExpenseUseCase(repository)
        
        // Add a test expense
        val expense = Expense(
            id = "test-cycle-1",
            amountPaise = 10000,
            category = Category.Food,
            merchant = "Test Merchant",
            paymentMethod = "UPI",
            timestamp = Clock.System.now()
        )
        repository.addExpense(expense)
        
        // Delete
        deleteUseCase("test-cycle-1")
        
        // Verify deleted
        val afterDelete = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val afterDeleteResult = kotlinx.coroutines.flow.first(afterDelete)
        assertTrue(afterDeleteResult.none { it.id == "test-cycle-1" })
        
        // Restore
        restoreUseCase(expense)
        
        // Verify restored
        val afterRestore = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val afterRestoreResult = kotlinx.coroutines.flow.first(afterRestore)
        assertTrue(afterRestoreResult.any { it.id == "test-cycle-1" })
        
        // Delete again
        deleteUseCase("test-cycle-1")
        
        // Verify deleted again
        val afterSecondDelete = repository.observeAll(
            com.example.myapplication.domain.model.TransactionFilter(),
            com.example.myapplication.domain.model.SortOrder.NewestFirst
        )
        val afterSecondDeleteResult = kotlinx.coroutines.flow.first(afterSecondDelete)
        assertTrue(afterSecondDeleteResult.none { it.id == "test-cycle-1" })
    }
}
