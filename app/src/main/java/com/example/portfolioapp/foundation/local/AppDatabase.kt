package com.example.portfolioapp.foundation.local

import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.entity.UserProfile

object AppDatabase {
    val photos = mutableListOf<Photo>()
    val profiles = mutableListOf<UserProfile>()
}
