package com.example.portfolioapp.foundation.repository

import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.entity.UserProfile
import com.example.portfolioapp.foundation.local.AppDatabase

class PortfolioRepository {

    fun getPhotos(): List<Photo> = AppDatabase.photos

    fun addPhoto(photo: Photo) {
        AppDatabase.photos.add(photo)
    }

    fun getProfiles(): List<UserProfile> = AppDatabase.profiles

    fun addProfile(profile: UserProfile) {
        AppDatabase.profiles.add(profile)
    }
}
