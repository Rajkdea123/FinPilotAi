package com.example.myapplication.ui.profile.edit

import com.example.myapplication.data.repository.FakeProfileRepository
import com.example.myapplication.domain.usecase.ValidateEmailUseCase
import com.example.myapplication.domain.usecase.ValidateNameUseCase
import com.example.myapplication.domain.usecase.ValidatePhoneUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditProfileViewModelTest {

    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var validateNameUseCase: ValidateNameUseCase
    private lateinit var validateEmailUseCase: ValidateEmailUseCase
    private lateinit var validatePhoneUseCase: ValidatePhoneUseCase
    private lateinit var viewModel: EditProfileViewModel

    @BeforeTest
    fun setup() {
        fakeProfileRepository = FakeProfileRepository()
        validateNameUseCase = ValidateNameUseCase()
        validateEmailUseCase = ValidateEmailUseCase()
        validatePhoneUseCase = ValidatePhoneUseCase()
        viewModel = EditProfileViewModel(
            fakeProfileRepository,
            validateNameUseCase,
            validateEmailUseCase,
            validatePhoneUseCase
        )
    }

    @Test
    fun `initial state has profile data and no changes`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("[Your name]", state.name)
        assertEquals("you@example.com", state.email)
        assertFalse(state.hasChanges)
    }

    @Test
    fun `name change sets hasChanges to true`() = runTest {
        viewModel.onEvent(EditProfileEvent.NameChanged("Jane Doe"))

        val state = viewModel.uiState.value
        assertEquals("Jane Doe", state.name)
        assertTrue(state.hasChanges)
    }

    @Test
    fun `validation failure prevents save`() = runTest {
        viewModel.onEvent(EditProfileEvent.NameChanged("   "))
        viewModel.onEvent(EditProfileEvent.SaveClicked)

        val state = viewModel.uiState.value
        assertEquals("Name cannot be blank", state.nameError)
        assertFalse(state.saveSuccess)
    }

    @Test
    fun `valid save updates profile and sets saveSuccess`() = runTest {
        viewModel.onEvent(EditProfileEvent.NameChanged("Jane Doe"))
        viewModel.onEvent(EditProfileEvent.SaveClicked)

        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertTrue(state.saveSuccess)
    }

    @Test
    fun `back clicked with changes shows discard dialog`() = runTest {
        viewModel.onEvent(EditProfileEvent.NameChanged("Jane Doe"))
        viewModel.onEvent(EditProfileEvent.BackClicked)

        val state = viewModel.uiState.value
        assertTrue(state.showDiscardDialog)
        assertFalse(state.navigateBack)

        viewModel.onEvent(EditProfileEvent.ConfirmDiscardClicked)
        val stateAfterConfirm = viewModel.uiState.value
        assertFalse(stateAfterConfirm.showDiscardDialog)
        assertTrue(stateAfterConfirm.navigateBack)
    }
}
