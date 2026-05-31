package com.example.portfolioapp

import android.content.Context
import android.util.Log
import com.example.portfolioapp.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TestNetwork {
    fun testLogin(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = AuthRequest(
                    email = "test@test.com",
                    password = "12345"
                )

                val response = RetrofitClient.api.login(request)

                if (response.isSuccessful) {
                    Log.d("TEST", "✅ Успех: ${response.body()}")
                } else {
                    Log.d("TEST", "❌ Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.d("TEST", "❌ Исключение: ${e.message}")
            }
        }
    }
}