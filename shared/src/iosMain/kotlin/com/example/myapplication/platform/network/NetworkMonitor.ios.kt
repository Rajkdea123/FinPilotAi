package com.example.myapplication.platform.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class NetworkMonitor actual constructor() {
    actual fun observe(): Flow<Boolean> = flowOf(true)
}

actual fun getNetworkMonitor(): NetworkMonitor = NetworkMonitor()
