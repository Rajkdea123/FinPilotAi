package com.example.myapplication.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SplashDestination {
    Onboarding,
    Login,
    Dashboard
}

class SplashViewModel : ViewModel() {
    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // TODO: Read auth token and onboarding flag from local storage
            // For now, always navigate to Onboarding
            delay(2200) // Wait for splash animations to complete
            _destination.value = SplashDestination.Onboarding
        }
    }
}
