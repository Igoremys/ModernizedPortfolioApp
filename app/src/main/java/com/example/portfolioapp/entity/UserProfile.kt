package com.example.portfolioapp.entity

import android.net.Uri

data class UserProfile(
    val id: Int,
    val name: String,
    val bio: String,
    val avatarUri: Uri? = null
)
