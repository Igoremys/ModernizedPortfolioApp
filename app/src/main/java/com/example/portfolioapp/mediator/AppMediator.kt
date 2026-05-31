package com.example.portfolioapp.mediator

import com.example.portfolioapp.control.PortfolioController
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.entity.UserProfile

class AppMediator(
    private val controller: PortfolioController = PortfolioController()
) {

    fun addDemoData() {
        if (controller.loadProfiles().isEmpty()) {
            controller.saveProfile(
                UserProfile(
                    id = 1,
                    name = "Demo User",
                    bio = "Photographer and mobile designer"
                )
            )
        }

        if (controller.loadPhotos().isEmpty()) {
            controller.savePhoto(
                Photo(
                    id = 1,
                    title = "First Photo",
                    description = "Demo portfolio image",
                    likes = 12,
                    authorName = "Demo User"
                )
            )
        }
    }
}
