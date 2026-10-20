package com.example.myapplication.ui.appearance

import com.example.myapplication.data.repository.FakeSettingsRepository
import com.example.myapplication.ui.theme.Accent
import com.example.myapplication.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceViewModelTest {

    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: AppearanceViewModel

    @BeforeTest
    fun setup() {
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = AppearanceViewModel(fakeSettingsRepository)
    }

    @Test
    fun `initial state loads saved settings`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(ThemeMode.System, state.themeMode)
        assertEquals(Accent.Mint, state.accent)
        assertEquals(ThemeMode.System, state.savedThemeMode)
        assertEquals(Accent.Mint, state.savedAccent)
    }

    @Test
    fun `theme and accent selection triggers live preview in settings repository`() = runTest {
        viewModel.onEvent(AppearanceEvent.ThemeSelected(ThemeMode.Dark))
        viewModel.onEvent(AppearanceEvent.AccentSelected(Accent.Violet))

        val state = viewModel.uiState.value
        assertEquals(ThemeMode.Dark, state.themeMode)
        assertEquals(Accent.Violet, state.accent)

        // Settings repository should reflect live preview
        val liveSettings = fakeSettingsRepository.observe().first()
        assertEquals(ThemeMode.Dark, liveSettings.themeMode)
        assertEquals(Accent.Violet, liveSettings.accentColor)
    }

    @Test
    fun `apply clicked persists saved settings and sets saveSuccess`() = runTest {
        viewModel.onEvent(AppearanceEvent.ThemeSelected(ThemeMode.Light))
        viewModel.onEvent(AppearanceEvent.AccentSelected(Accent.Coral))
        viewModel.onEvent(AppearanceEvent.ApplyClicked)

        val state = viewModel.uiState.value
        assertEquals(ThemeMode.Light, state.savedThemeMode)
        assertEquals(Accent.Coral, state.savedAccent)
        assertTrue(state.saveSuccess)

        val persisted = fakeSettingsRepository.observe().first()
        assertEquals(ThemeMode.Light, persisted.themeMode)
        assertEquals(Accent.Coral, persisted.accentColor)
    }

    @Test
    fun `back clicked without apply reverts settings repository to saved values`() = runTest {
        viewModel.onEvent(AppearanceEvent.ThemeSelected(ThemeMode.Dark))
        viewModel.onEvent(AppearanceEvent.AccentSelected(Accent.Sky))

        // Leaving without apply
        viewModel.onEvent(AppearanceEvent.BackClicked)

        val state = viewModel.uiState.value
        assertTrue(state.navigateBack)

        val reverted = fakeSettingsRepository.observe().first()
        assertEquals(ThemeMode.System, reverted.themeMode) // Reverted to saved System
        assertEquals(Accent.Mint, reverted.accentColor) // Reverted to saved Mint
    }
}
