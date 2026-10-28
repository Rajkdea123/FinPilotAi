package com.example.myapplication.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.platform.storage.UserPreferencesStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class OnboardingEvent {
    object Finished : OnboardingEvent()
}

class OnboardingViewModel : ViewModel(), KoinComponent {
    private val userPreferencesStorage: UserPreferencesStorage by inject()
    
    private val _event = MutableStateFlow<OnboardingEvent?>(null)
    val event: StateFlow<OnboardingEvent?> = _event.asStateFlow()

    fun onSkip() {
        viewModelScope.launch {
            userPreferencesStorage.setOnboardingCompleted(true)
            _event.value = OnboardingEvent.Finished
        }
    }

    fun onFinished() {
        viewModelScope.launch {
            userPreferencesStorage.setOnboardingCompleted(true)
            _event.value = OnboardingEvent.Finished
        }
    }
}
