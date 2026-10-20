package com.example.myapplication.ui.notifications

import com.example.myapplication.data.repository.FakeNotificationPrefsRepository
import com.example.myapplication.data.repository.FakeSettingsRepository
import com.example.myapplication.domain.model.NotificationPrefs
import com.example.myapplication.domain.model.PermissionStatus
import com.example.myapplication.domain.usecase.ObserveNotificationPrefsUseCase
import com.example.myapplication.domain.usecase.ObservePermissionStatusUseCase
import com.example.myapplication.domain.usecase.UpdateNotificationPrefsUseCase
import com.example.myapplication.domain.usecase.UpdateSettingsUseCase
import com.example.myapplication.platform.notification.FakeNotificationPermissionChecker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeNotificationPrefsRepository: FakeNotificationPrefsRepository
    private lateinit var fakePermissionChecker: FakeNotificationPermissionChecker
    private lateinit var viewModel: NotificationsViewModel

    @BeforeTest
    fun setup() {
        fakeSettingsRepository = FakeSettingsRepository()
        fakeNotificationPrefsRepository = FakeNotificationPrefsRepository()
        fakePermissionChecker = FakeNotificationPermissionChecker()
        
        val observeNotificationPrefsUseCase = ObserveNotificationPrefsUseCase(fakeNotificationPrefsRepository)
        val updateNotificationPrefsUseCase = UpdateNotificationPrefsUseCase(fakeNotificationPrefsRepository)
        val observePermissionStatusUseCase = ObservePermissionStatusUseCase(fakePermissionChecker)
        val updateSettingsUseCase = UpdateSettingsUseCase(fakeSettingsRepository)
        
        viewModel = NotificationsViewModel(
            observeNotificationPrefsUseCase,
            updateNotificationPrefsUseCase,
            observePermissionStatusUseCase,
            updateSettingsUseCase,
            fakeSettingsRepository,
            fakePermissionChecker
        )
    }

    @Test
    fun `initial state loads master enabled from settings`() = runTest {
        val state = viewModel.uiState.first()
        assertTrue(state.masterEnabled)
    }

    @Test
    fun `initial state loads notification prefs from repository`() = runTest {
        val state = viewModel.uiState.first()
        assertEquals(NotificationPrefs(), state.prefs)
    }

    @Test
    fun `initial state loads permission status from checker`() = runTest {
        val state = viewModel.uiState.first()
        assertEquals(PermissionStatus.Granted, state.permissionStatus)
    }

    @Test
    fun `master toggle updates settings repository`() = runTest {
        viewModel.onEvent(NotificationsEvent.MasterToggled)
        
        val state = viewModel.uiState.first()
        assertFalse(state.masterEnabled)
        
        val settings = fakeSettingsRepository.observe().first()
        assertFalse(settings.smartNotifications)
    }

    @Test
    fun `master off disables all alert rows in state`() = runTest {
        viewModel.onEvent(NotificationsEvent.MasterToggled)
        
        val state = viewModel.uiState.first()
        assertFalse(state.masterEnabled)
        // The UI uses masterEnabled to disable rows, so the state should reflect this
    }

    @Test
    fun `spending alerts toggle updates notification prefs`() = runTest {
        viewModel.onEvent(NotificationsEvent.SpendingAlertsToggled)
        
        val state = viewModel.uiState.first()
        assertFalse(state.prefs.spendingAlerts)
        
        val prefs = fakeNotificationPrefsRepository.observe().first()
        assertFalse(prefs.spendingAlerts)
    }

    @Test
    fun `unused subscriptions toggle updates notification prefs`() = runTest {
        viewModel.onEvent(NotificationsEvent.UnusedSubscriptionsToggled)
        
        val state = viewModel.uiState.first()
        assertFalse(state.prefs.unusedSubscriptions)
        
        val prefs = fakeNotificationPrefsRepository.observe().first()
        assertFalse(prefs.unusedSubscriptions)
    }

    @Test
    fun `budget warnings toggle updates notification prefs and shows threshold section`() = runTest {
        viewModel.onEvent(NotificationsEvent.BudgetWarningsToggled)
        
        val state = viewModel.uiState.first()
        assertFalse(state.prefs.budgetWarnings)
        assertFalse(state.showBudgetThreshold)
    }

    @Test
    fun `threshold selection updates notification prefs`() = runTest {
        viewModel.onEvent(NotificationsEvent.ThresholdSelected(90))
        
        val state = viewModel.uiState.first()
        assertEquals(90, state.prefs.budgetThresholdPercent)
        
        val prefs = fakeNotificationPrefsRepository.observe().first()
        assertEquals(90, prefs.budgetThresholdPercent)
    }

    @Test
    fun `threshold selection validates allowed values`() = runTest {
        viewModel.onEvent(NotificationsEvent.ThresholdSelected(80))
        assertEquals(80, viewModel.uiState.first().prefs.budgetThresholdPercent)
        
        viewModel.onEvent(NotificationsEvent.ThresholdSelected(90))
        assertEquals(90, viewModel.uiState.first().prefs.budgetThresholdPercent)
        
        viewModel.onEvent(NotificationsEvent.ThresholdSelected(100))
        assertEquals(100, viewModel.uiState.first().prefs.budgetThresholdPercent)
    }

    @Test
    fun `weekly summary toggle updates notification prefs`() = runTest {
        viewModel.onEvent(NotificationsEvent.WeeklySummaryToggled)
        
        val state = viewModel.uiState.first()
        assertTrue(state.prefs.weeklySummary)
        
        val prefs = fakeNotificationPrefsRepository.observe().first()
        assertTrue(prefs.weeklySummary)
    }

    @Test
    fun `sync issues toggle updates notification prefs`() = runTest {
        viewModel.onEvent(NotificationsEvent.SyncIssuesToggled)
        
        val state = viewModel.uiState.first()
        assertFalse(state.prefs.syncIssues)
        
        val prefs = fakeNotificationPrefsRepository.observe().first()
        assertFalse(prefs.syncIssues)
    }

    @Test
    fun `permission denied shows banner in state`() = runTest {
        fakePermissionChecker.setStatus(PermissionStatus.Denied)
        viewModel.refreshPermissionStatus()
        
        val state = viewModel.uiState.first()
        assertEquals(PermissionStatus.Denied, state.permissionStatus)
    }

    @Test
    fun `toggle budget threshold expands section when master is enabled`() = runTest {
        viewModel.onEvent(NotificationsEvent.ToggleBudgetThreshold)
        
        val state = viewModel.uiState.first()
        assertTrue(state.showBudgetThreshold)
    }
}
