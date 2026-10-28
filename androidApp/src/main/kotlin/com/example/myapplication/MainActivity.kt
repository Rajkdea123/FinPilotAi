package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.db.DatabaseDriverFactory
import com.example.myapplication.di.initKoin
import com.example.myapplication.platform.storage.SecureTokenStorage
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize DatabaseDriverFactory with context
        val driverFactory = DatabaseDriverFactory()
        driverFactory.init(this)
        
        // Initialize SecureTokenStorage with context
        val tokenStorage = SecureTokenStorage()
        tokenStorage.init(this)
        
        // Initialize Koin for Android
        initKoin()

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}