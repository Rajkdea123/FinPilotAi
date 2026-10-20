package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeProfileRepository
import com.example.myapplication.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetProfileUseCaseTest {

    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var getProfileUseCase: GetProfileUseCase

    @BeforeTest
    fun setup() {
        fakeProfileRepository = FakeProfileRepository()
        getProfileUseCase = GetProfileUseCase(fakeProfileRepository)
    }

    @Test
    fun `invoke returns user profile`() = runTest {
        val profile = getProfileUseCase().first()

        assertNotNull(profile)
        assertEquals("user-1", profile.id)
        assertEquals("[Your name]", profile.name)
        assertEquals("you@example.com", profile.email)
        assertEquals(null, profile.avatarUrl)
    }
}
