package com.example.portfolioapp.network

import android.content.Context

object TokenManager {

    private var cachedToken: String? = null
    private var cachedUserId: Long = -1

    fun saveToken(context: Context, token: String, userId: Long) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("access_token", token)
            .putLong("user_id", userId)
            .apply()

        // ✅ Кэшируем в памяти для интерцептора
        cachedToken = token
        cachedUserId = userId
    }

    // ✅ Геттер с Context (для ViewModel)
    fun getToken(context: Context): String? {
        return cachedToken ?: run {
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            prefs.getString("access_token", null)?.also { cachedToken = it }
        }
    }

    // ✅ Геттер БЕЗ Context (для интерцептора в RetrofitClient)
    fun getToken(): String? = cachedToken

    fun getUserId(context: Context): Long {
        return if (cachedUserId != -1L) {
            cachedUserId
        } else {
            val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            prefs.getLong("user_id", -1).also { cachedUserId = it }
        }
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        cachedToken = null
        cachedUserId = -1
    }
}