package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeProfileRepository
import com.example.myapplication.domain.model.ProfileStats
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetProfileStatsUseCaseTest {

    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var getProfileStatsUseCase: GetProfileStatsUseCase

    @BeforeTest
    fun setup() {
        fakeProfileRepository = FakeProfileRepository()
        getProfileStatsUseCase = GetProfileStatsUseCase(fakeProfileRepository)
    }

    @Test
    fun `invoke returns profile stats`() = runTest {
        val stats = getProfileStatsUseCase().first()

        assertNotNull(stats)
        assertEquals(124, stats.expenseCount)
        assertEquals(5, stats.categoryCount)
        assertEquals(2, stats.deviceCount)
    }
}
