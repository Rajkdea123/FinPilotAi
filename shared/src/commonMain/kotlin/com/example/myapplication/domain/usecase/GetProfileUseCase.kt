package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class GetProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<UserProfile> {
        return profileRepository.observeProfile()
    }
}
