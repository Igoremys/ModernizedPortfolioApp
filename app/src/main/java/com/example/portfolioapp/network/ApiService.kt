package com.example.portfolioapp.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @GET("photos")
    suspend fun getAllPhotos(@Header("Authorization") token: String): Response<List<PhotoDto>>

    @POST("photos")
    suspend fun createPhoto(
        @Body photo: PhotoCreateRequest,
        @Header("Authorization") token: String
    ): Response<PhotoDto>

    @GET("photos/{id}")
    suspend fun getPhotoById(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<PhotoDto>

    @DELETE("photos/{id}")
    suspend fun deletePhoto(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Unit>
}