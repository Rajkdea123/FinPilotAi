package com.example.myapplication.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OnboardingEvent {
    object Finished : OnboardingEvent()
}

class OnboardingViewModel : ViewModel() {
    private val _event = MutableStateFlow<OnboardingEvent?>(null)
    val event: StateFlow<OnboardingEvent?> = _event.asStateFlow()

    fun onSkip() {
        viewModelScope.launch {
            // TODO: Save "onboardingCompleted = true" in local storage
            _event.value = OnboardingEvent.Finished
        }
    }

    fun onFinished() {
        viewModelScope.launch {
            // TODO: Save "onboardingCompleted = true" in local storage
            _event.value = OnboardingEvent.Finished
        }
    }
}
