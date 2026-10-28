package com.example.myapplication.db

import platform.Foundation.NSBundle

actual val isDebugBuild: Boolean {
    return NSBundle.mainBundle.objectForInfoDictionaryKey("Debug") as? Boolean == true
}
