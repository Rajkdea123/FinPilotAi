package com.example.myapplication.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory {
    private lateinit var context: Context
    
    fun init(context: Context) {
        this.context = context
    }
    
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(FinPilotDatabase.Schema, context, "FinPilotDatabase.db")
    }
}
