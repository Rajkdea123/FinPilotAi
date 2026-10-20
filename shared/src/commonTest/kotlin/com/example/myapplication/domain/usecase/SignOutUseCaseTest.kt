package com.example.myapplication.domain.usecase

import com.example.myapplication.data.repository.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SignOutUseCaseTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var signOutUseCase: SignOutUseCase

    @BeforeTest
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        signOutUseCase = SignOutUseCase(fakeAuthRepository)
    }

    @Test
    fun `invoke calls signOut on repository`() = runTest {
        signOutUseCase()
        // TODO: Add assertion when secure storage is implemented
    }
}
