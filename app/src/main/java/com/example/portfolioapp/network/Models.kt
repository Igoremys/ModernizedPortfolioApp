package com.example.portfolioapp.network

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    val email: String,
    val password: String,
    val fullName: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val user: UserDto
)

data class UserDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val role: String,
    val avatarUrl: String? = null,
    val description: String? = null
)

// ✅ PhotoDto: добавляем fallback-поля на случай, если сервер меняет структуру
data class PhotoDto(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String,
    // ✅ Если сервер отдаёт authorName вместо author — используем @SerializedName
    @SerializedName("author") val author: String = "",
    @SerializedName("authorName") val authorNameFallback: String? = null,
    val createdAt: String,
    val updatedAt: String
) {
    // ✅ Вспомогательное свойство: возвращает корректное имя автора
    val resolvedAuthor: String
        get() = authorNameFallback?.takeIf { it.isNotBlank() } ?: author.takeIf { it.isNotBlank() && !it.contains("@") } ?: author.substringBefore("@", "Пользователь")
}

data class PhotoCreateRequest(
    val title: String,
    val description: String,
    val imageUrl: String
)

data class ProfileUpdateRequest(
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("description") val description: String? = null
)