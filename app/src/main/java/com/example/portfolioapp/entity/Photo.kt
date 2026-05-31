package com.example.portfolioapp.entity

import android.net.Uri

data class Photo(
    val id: Int,
    val title: String,
    val description: String,
    val uri: Uri? = null,
    var likes: Int = 0,
    var isLiked: Boolean = false,
    val authorName: String = "",
    val authorAvatarUri: Uri? = null
)
