package com.example.myapplication.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?
)

data class ProfileStats(
    val expenseCount: Int,
    val categoryCount: Int,
    val deviceCount: Int
)
