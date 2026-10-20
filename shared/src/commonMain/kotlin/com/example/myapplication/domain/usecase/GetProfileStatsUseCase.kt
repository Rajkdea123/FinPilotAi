package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.ProfileStats
import com.example.myapplication.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class GetProfileStatsUseCase(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<ProfileStats> {
        return profileRepository.observeStats()
    }
}
