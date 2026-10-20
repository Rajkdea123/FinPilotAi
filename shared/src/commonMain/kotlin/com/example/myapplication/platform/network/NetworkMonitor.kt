package com.example.myapplication.platform.network

import kotlinx.coroutines.flow.Flow

expect class NetworkMonitor() {
    fun observe(): Flow<Boolean>
}

expect fun getNetworkMonitor(): NetworkMonitor
