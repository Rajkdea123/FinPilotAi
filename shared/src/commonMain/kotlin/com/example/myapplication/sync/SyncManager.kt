package com.example.myapplication.sync

import com.example.myapplication.di.AppCoroutineDispatchers
import com.example.myapplication.platform.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SyncManager(
    private val syncEngine: SyncEngine,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: AppCoroutineDispatchers
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    
    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    init {
        // Observe network connectivity and trigger sync when online
        networkMonitor.observe()
            .onEach { isOnline ->
                _syncStatus.value = _syncStatus.value.copy(isOnline = isOnline)
                if (isOnline) {
                    triggerSync()
                }
            }
            .launchIn(scope)
    }
    
    fun triggerSync() {
        scope.launch {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
            try {
                val result = syncEngine.syncPendingChanges()
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    pendingChanges = result.failureCount
                )
            } catch (e: Exception) {
                _syncStatus.value = _syncStatus.value.copy(isSyncing = false)
            }
        }
    }
}

data class SyncStatus(
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val pendingChanges: Int = 0
)
