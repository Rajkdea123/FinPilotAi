package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.PermissionStatus
import com.example.myapplication.platform.notification.FakeNotificationPermissionChecker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObservePermissionStatusUseCaseTest {

    private lateinit var fakeChecker: FakeNotificationPermissionChecker
    private lateinit var useCase: ObservePermissionStatusUseCase

    @BeforeTest
    fun setup() {
        fakeChecker = FakeNotificationPermissionChecker()
        useCase = ObservePermissionStatusUseCase(fakeChecker)
    }

    @Test
    fun `initial status is Granted`() = runTest {
        val status = useCase().first()
        assertEquals(PermissionStatus.Granted, status)
    }

    @Test
    fun `observe emits updated status after change`() = runTest {
        fakeChecker.setStatus(PermissionStatus.Denied)
        
        val status = useCase().first()
        assertEquals(PermissionStatus.Denied, status)
    }

    @Test
    fun `observe emits NotDetermined status`() = runTest {
        fakeChecker.setStatus(PermissionStatus.NotDetermined)
        
        val status = useCase().first()
        assertEquals(PermissionStatus.NotDetermined, status)
    }

    @Test
    fun `observe emits multiple status changes`() = runTest {
        fakeChecker.setStatus(PermissionStatus.NotDetermined)
        assertEquals(PermissionStatus.NotDetermined, useCase().first())
        
        fakeChecker.setStatus(PermissionStatus.Granted)
        assertEquals(PermissionStatus.Granted, useCase().first())
        
        fakeChecker.setStatus(PermissionStatus.Denied)
        assertEquals(PermissionStatus.Denied, useCase().first())
    }
}
