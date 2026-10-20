package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.signOut()
    }
}
