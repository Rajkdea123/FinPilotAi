package com.example.myapplication.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory actual constructor() {
    private var context: Context? = null

    constructor(context: Context?) : this() {
        this.context = context
        context?.let { appContext = it.applicationContext }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun createDriver(): SqlDriver {
        val ctx = appContext ?: context ?: throw IllegalStateException("DatabaseDriverFactory not initialized with Context")
        return AndroidSqliteDriver(FinPilotDatabase.Schema, ctx, "finpilot.db")
    }

    companion object {
        private var appContext: Context? = null
    }
}
