package com.example.portfolioapp.network

import com.google.gson.annotations.SerializedName

// 🔐 Запрос авторизации
data class AuthRequest(
    val email: String,
    val password: String,
    val fullName: String? = null
)

// 🔐 Ответ авторизации
data class AuthResponse(
    val accessToken: String,
    val user: UserDto
)

// 👤 Данные пользователя
data class UserDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val role: String,
    val avatarUrl: String? = null,
    val description: String? = null
)

// 📸 Фотография (ответ от сервера)
data class PhotoDto(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val createdAt: String,
    val updatedAt: String
)

// 📸 Запрос создания фото
data class PhotoCreateRequest(
    val title: String,
    val description: String,
    val imageUrl: String
)

// ✅ Запрос обновления профиля (ТОЛЬКО ОДИН РАЗ!)
data class ProfileUpdateRequest(
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("description") val description: String? = null
)