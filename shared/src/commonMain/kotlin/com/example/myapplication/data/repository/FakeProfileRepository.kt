package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.ProfileStats
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeProfileRepository : ProfileRepository {
    // TODO: Replace with SQLDelight and Ktor implementation
    // TODO: Derive expense and category counts from ExpenseRepository if easy

    private val _profile = MutableStateFlow(
        UserProfile(
            id = "user-1",
            name = "[Your name]",
            email = "you@example.com",
            avatarUrl = null
        )
    )

    override fun observeProfile(): Flow<UserProfile> {
        return _profile
    }

    override fun observeStats(): Flow<ProfileStats> {
        return flowOf(
            ProfileStats(
                expenseCount = 124,
                categoryCount = 5,
                deviceCount = 2
            )
        )
    }

    override suspend fun update(profile: UserProfile): Result<Unit> {
        // TODO: Replace with SQLDelight and Ktor implementation
        _profile.value = profile
        return Result.success(Unit)
    }
}
