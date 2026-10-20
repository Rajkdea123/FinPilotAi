package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.ProfileStats
import com.example.myapplication.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    fun observeStats(): Flow<ProfileStats>
    suspend fun update(profile: UserProfile): Result<Unit>
}
