package com.example.myapplication.ui.profile.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.repository.ProfileRepository
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
import com.example.myapplication.domain.usecase.ValidateNameUseCase
import com.example.myapplication.domain.usecase.ValidatePhoneUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val currency: String = "Indian Rupee (₹)",
    val avatarUrl: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val showPhotoSnackbar: Boolean = false,
    val saveSuccess: Boolean = false,
    val currencySheetOpen: Boolean = false,
    val navigateBack: Boolean = false
)

sealed interface EditProfileEvent {
    data class NameChanged(val name: String) : EditProfileEvent
    data class EmailChanged(val email: String) : EditProfileEvent
    data class PhoneChanged(val phone: String) : EditProfileEvent
    data class CurrencySelected(val currency: String) : EditProfileEvent
    object ChangePhotoClicked : EditProfileEvent
    object PhotoSnackbarDismissed : EditProfileEvent
    object SaveClicked : EditProfileEvent
    object BackClicked : EditProfileEvent
    object ConfirmDiscardClicked : EditProfileEvent
    object DismissDiscardClicked : EditProfileEvent
    object OpenCurrencySheet : EditProfileEvent
    object DismissCurrencySheet : EditProfileEvent
}

class EditProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val validateNameUseCase: ValidateNameUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePhoneUseCase: ValidatePhoneUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var initialProfile: UserProfile? = null

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = profileRepository.observeProfile().firstOrNull() ?: UserProfile("user-1", "", "", null)
            initialProfile = profile
            _uiState.update {
                it.copy(
                    name = profile.name,
                    email = profile.email,
                    phone = "",
                    avatarUrl = profile.avatarUrl,
                    hasChanges = false
                )
            }
        }
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.NameChanged -> {
                _uiState.update {
                    val updated = it.copy(name = event.name, nameError = null)
                    updated.copy(hasChanges = checkHasChanges(updated))
                }
            }

            is EditProfileEvent.EmailChanged -> {
                _uiState.update {
                    val updated = it.copy(email = event.email, emailError = null)
                    updated.copy(hasChanges = checkHasChanges(updated))
                }
            }

            is EditProfileEvent.PhoneChanged -> {
                _uiState.update {
                    val updated = it.copy(phone = event.phone, phoneError = null)
                    updated.copy(hasChanges = checkHasChanges(updated))
                }
            }

            is EditProfileEvent.CurrencySelected -> {
                _uiState.update {
                    val updated = it.copy(currency = event.currency, currencySheetOpen = false)
                    updated.copy(hasChanges = checkHasChanges(updated))
                }
            }

            EditProfileEvent.ChangePhotoClicked -> {
                _uiState.update { it.copy(showPhotoSnackbar = true) }
            }

            EditProfileEvent.PhotoSnackbarDismissed -> {
                _uiState.update { it.copy(showPhotoSnackbar = false) }
            }

            EditProfileEvent.OpenCurrencySheet -> {
                _uiState.update { it.copy(currencySheetOpen = true) }
            }

            EditProfileEvent.DismissCurrencySheet -> {
                _uiState.update { it.copy(currencySheetOpen = false) }
            }

            EditProfileEvent.BackClicked -> {
                if (_uiState.value.hasChanges) {
                    _uiState.update { it.copy(showDiscardDialog = true) }
                } else {
                    _uiState.update { it.copy(navigateBack = true) }
                }
            }

            EditProfileEvent.ConfirmDiscardClicked -> {
                _uiState.update { it.copy(showDiscardDialog = false, navigateBack = true) }
            }

            EditProfileEvent.DismissDiscardClicked -> {
                _uiState.update { it.copy(showDiscardDialog = false) }
            }

            EditProfileEvent.SaveClicked -> {
                saveProfile()
            }
        }
    }

    private fun checkHasChanges(state: EditProfileUiState): Boolean {
        val initial = initialProfile ?: return false
        return state.name != initial.name ||
                state.email != initial.email ||
                state.phone.isNotBlank() ||
                state.currency != "Indian Rupee (₹)"
    }

    private fun saveProfile() {
        val nameResult = validateNameUseCase(_uiState.value.name)
        val emailResult = validateEmailUseCase(_uiState.value.email)
        val phoneResult = validatePhoneUseCase(_uiState.value.phone)

        if (!nameResult.isValid || !emailResult.isValid || !phoneResult.isValid) {
            _uiState.update {
                it.copy(
                    nameError = nameResult.errorMessage,
                    emailError = emailResult.errorMessage,
                    phoneError = phoneResult.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val current = _uiState.value
            val id = initialProfile?.id ?: "user-1"
            val result = profileRepository.update(
                UserProfile(
                    id = id,
                    name = current.name,
                    email = current.email,
                    avatarUrl = current.avatarUrl
                )
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } else {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
