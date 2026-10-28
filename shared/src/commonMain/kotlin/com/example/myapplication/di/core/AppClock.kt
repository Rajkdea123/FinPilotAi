package com.example.myapplication.di.core

import kotlinx.datetime.Clock

interface AppClock {
    fun now(): kotlinx.datetime.Instant
}

class DefaultAppClock : AppClock {
    override fun now(): kotlinx.datetime.Instant = Clock.System.now()
}
