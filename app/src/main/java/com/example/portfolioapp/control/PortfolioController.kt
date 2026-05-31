package com.example.portfolioapp.control

import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.entity.UserProfile
import com.example.portfolioapp.foundation.repository.PortfolioRepository

class PortfolioController(
    private val repository: PortfolioRepository = PortfolioRepository()
) {

    fun loadPhotos(): List<Photo> {
        return repository.getPhotos()
    }

    fun savePhoto(photo: Photo) {
        repository.addPhoto(photo)
    }

    fun loadProfiles(): List<UserProfile> {
        return repository.getProfiles()
    }

    fun saveProfile(profile: UserProfile) {
        repository.addProfile(profile)
    }
}
