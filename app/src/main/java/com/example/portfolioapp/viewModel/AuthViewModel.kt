package com.example.portfolioapp.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Состояния загрузки и ошибок
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ✅ ГЛАВНОЕ: Храним данные текущего пользователя
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser

    private val _authSuccess = MutableStateFlow<AuthResponse?>(null)
    val authSuccess: StateFlow<AuthResponse?> = _authSuccess

    // 🔥 МЕТОД РЕГИСТРАЦИИ
    fun register(email: String, password: String, fullName: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val request = AuthRequest(email, password, fullName)
                val response = RetrofitClient.api.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // Сохраняем токен и ID
                    TokenManager.saveToken(context, authResponse.accessToken, authResponse.user.id)
                    // ✅ Сохраняем данные пользователя в ViewModel
                    _currentUser.value = authResponse.user
                    _authSuccess.value = authResponse
                } else {
                    _error.value = "Ошибка регистрации: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ МЕТОД ВХОДА
    fun login(email: String, password: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val request = AuthRequest(email, password)
                val response = RetrofitClient.api.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    TokenManager.saveToken(context, authResponse.accessToken, authResponse.user.id)
                    // ✅ Сохраняем данные пользователя в ViewModel
                    _currentUser.value = authResponse.user
                    _authSuccess.value = authResponse
                } else {
                    _error.value = "Ошибка: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ МЕТОД ВЫХОДА
    fun logout(context: Context) {
        TokenManager.clearToken(context)
        _currentUser.value = null
        _authSuccess.value = null
    }

    // ✅ Загрузка данных пользователя из токена (при старте приложения)
    fun loadCurrentUser(context: Context) {
        val token = TokenManager.getToken(context)
        val userId = TokenManager.getUserId(context)

        if (token != null && userId != -1L) {
            // Пока создаём заглушку, т.к. нет эндпоинта /api/users/{id}
            // В будущем здесь будет запрос к серверу
            _currentUser.value = UserDto(
                id = userId,
                email = "",
                fullName = "User $userId",
                role = "USER"
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}