package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeSyncRepository
import com.example.myapplication.domain.model.ConflictChoice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResolveConflictUseCaseTest {

    private lateinit var fakeSyncRepository: FakeSyncRepository
    private lateinit var resolveConflictUseCase: ResolveConflictUseCase

    @BeforeTest
    fun setup() {
        fakeSyncRepository = FakeSyncRepository()
        resolveConflictUseCase = ResolveConflictUseCase(fakeSyncRepository)
    }

    @Test
    fun `invoke removes conflict from list`() = runTest {
        val conflicts = fakeSyncRepository.observeConflicts().first()
        assertEquals(1, conflicts.size)

        resolveConflictUseCase(conflicts[0].id, ConflictChoice.Latest)

        val updatedConflicts = fakeSyncRepository.observeConflicts().first()
        assertTrue(updatedConflicts.isEmpty())
    }
}
