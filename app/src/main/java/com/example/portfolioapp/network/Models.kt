package com.example.portfolioapp.network

// Запрос авторизации
data class AuthRequest(
    val email: String,
    val password: String,
    val fullName: String? = null
)

// Ответ авторизации
data class AuthResponse(
    val accessToken: String,
    val user: UserDto
)

data class UserDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val role: String
)

// Фотография (ответ от сервера)
data class PhotoDto(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val createdAt: String,
    val updatedAt: String
)

// Запрос создания фото (без id и дат)
data class PhotoCreateRequest(
    val title: String,
    val description: String,
    val imageUrl: String
)