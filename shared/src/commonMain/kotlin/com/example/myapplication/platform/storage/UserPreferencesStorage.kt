package com.example.myapplication.platform.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface UserPreferencesStorage {
    val onboardingCompleted: Flow<Boolean>
    val isSignedIn: Flow<Boolean>
    
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setSignedIn(signedIn: Boolean)
    suspend fun clearAll()
}

class InMemoryUserPreferencesStorage : UserPreferencesStorage {
    private val _onboardingCompleted = MutableStateFlow(false)
    private val _isSignedIn = MutableStateFlow(false)
    
    override val onboardingCompleted: Flow<Boolean> = _onboardingCompleted.asStateFlow()
    override val isSignedIn: Flow<Boolean> = _isSignedIn.asStateFlow()
    
    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
    }
    
    override suspend fun setSignedIn(signedIn: Boolean) {
        _isSignedIn.value = signedIn
    }
    
    override suspend fun clearAll() {
        _onboardingCompleted.value = false
        _isSignedIn.value = false
    }
}
