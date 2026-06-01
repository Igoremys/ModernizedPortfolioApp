package com.example.portfolioapp.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface PhotoApi {
    @Multipart
    @POST("photos/upload") // BASE_URL уже заканчивается на /api/, поэтому пишем photos/upload
    suspend fun uploadPhoto(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>
}