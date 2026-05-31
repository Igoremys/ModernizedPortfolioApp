package com.example.portfolioapp.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 🔐 Auth (публичные методы — токен не нужен)
    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    // 👤 User Profile (✅ ТОКЕН ДОБАВЛЯЕТ ИНТЕРЦЕПТОР!)
    @PUT("users/profile")
    suspend fun updateProfile(
        @Body request: ProfileUpdateRequest
    ): Response<UserDto>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    // 📸 Photos (✅ ТОКЕН ДОБАВЛЯЕТ ИНТЕРЦЕПТОР!)
    @GET("photos")
    suspend fun getAllPhotos(): Response<List<PhotoDto>>

    @POST("photos")
    suspend fun createPhoto(
        @Body photo: PhotoCreateRequest
    ): Response<PhotoDto>

    @GET("photos/{id}")
    suspend fun getPhotoById(
        @Path("id") id: Long
    ): Response<PhotoDto>

    @DELETE("photos/{id}")
    suspend fun deletePhoto(
        @Path("id") id: Long
    ): Response<Unit>

    // ✅ НОВОЕ: Загрузка файла (аватар/фото) на сервер
    @Multipart
    @POST("photos/upload")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,  // ← Токен передаём вручную, так как это Multipart
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>  // ← Ожидаем ответ: {"url": "http://..."}
}