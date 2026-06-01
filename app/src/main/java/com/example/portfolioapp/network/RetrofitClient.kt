package com.example.portfolioapp.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ✅ Public const для формирования полных URL в UI (Coil/Glide)
    // Для эмулятора: 10.0.2.2, для реального устройства: 192.168.X.X
    const val BASE_URL = "http://192.168.0.33:8080/api/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ✅ AuthInterceptor: автоматически добавляет JWT ко всем запросам
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
        TokenManager.getToken()?.let { token ->
            request.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(request.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor) // ← Ключевое исправление!
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ✅ Основной API-интерфейс (с авто-подстановкой токена)
    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }

    // ✅ Отдельный экземпляр для PhotoApi (если нужен ручной контроль токена в multipart)
    fun getPhotoApi(): PhotoApi = retrofit.create(PhotoApi::class.java)
}