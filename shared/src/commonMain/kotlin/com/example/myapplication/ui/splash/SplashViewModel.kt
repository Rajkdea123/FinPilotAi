package com.example.myapplication.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.platform.storage.UserPreferencesStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class SplashDestination {
    Onboarding,
    Login,
    Dashboard
}

class SplashViewModel : ViewModel(), KoinComponent {
    private val userPreferencesStorage: UserPreferencesStorage by inject()
    
    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // Read auth token and onboarding flag from local storage
            val onboardingCompleted = userPreferencesStorage.onboardingCompleted
            val isSignedIn = userPreferencesStorage.isSignedIn
            
            delay(2200) // Wait for splash animations to complete
            
            // Collect first value from flows
            val onboardingValue = onboardingCompleted.first()
            val signedInValue = isSignedIn.first()
            
            val destination = when {
                !onboardingValue -> SplashDestination.Onboarding
                !signedInValue -> SplashDestination.Login
                else -> SplashDestination.Dashboard
            }
            _destination.value = destination
        }
    }
}
