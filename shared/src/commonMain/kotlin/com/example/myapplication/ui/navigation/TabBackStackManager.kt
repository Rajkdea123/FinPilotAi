package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myapplication.ui.navigation.Route

@Composable
fun rememberTabBackStackManager(): TabBackStackManager {
    return remember { TabBackStackManager() }
}

class TabBackStackManager {
    private val tabBackStacks = mutableMapOf<Route, MutableList<Route>>()
    private var currentTab: Route = Route.Dashboard
    
    fun setCurrentTab(tab: Route) {
        currentTab = tab
        // Initialize back stack for tab if not exists
        if (!tabBackStacks.containsKey(tab)) {
            tabBackStacks[tab] = mutableListOf()
        }
    }
    
    fun getCurrentTab(): Route = currentTab
    
    fun getCurrentBackStack(): List<Route> {
        return tabBackStacks[currentTab] ?: emptyList()
    }
    
    fun push(route: Route) {
        val backStack = tabBackStacks[currentTab] ?: mutableListOf()
        backStack.add(route)
        tabBackStacks[currentTab] = backStack
    }
    
    fun pop(): Route? {
        val backStack = tabBackStacks[currentTab] ?: return null
        if (backStack.isEmpty()) return null
        return backStack.removeLast()
    }
    
    fun clearCurrentBackStack() {
        tabBackStacks[currentTab] = mutableListOf()
    }
    
    fun clearAll() {
        tabBackStacks.clear()
        currentTab = Route.Dashboard
    }
}
