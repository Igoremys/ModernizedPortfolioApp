package com.example.portfolioapp.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Данные текущего пользователя
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser

    // Список фото
    private val _photos = MutableStateFlow<List<PhotoDto>>(emptyList())
    val photos: StateFlow<List<PhotoDto>> = _photos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Инициализация (вызывать после входа)
    fun initialize(context: Context) {
        val token = TokenManager.getToken(context)
        val userId = TokenManager.getUserId(context)

        if (token != null && userId != -1L) {
            _currentUser.value = UserDto(
                id = userId,
                email = "",
                fullName = "",
                role = ""
            )
            loadPhotos()
        }
    }

    // Загрузка фото с сервера
    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: Здесь загрузка фото с сервера через Retrofit
            // Пока оставляем пустым
        }
    }

    // Выход
    fun logout(context: Context) {
        TokenManager.clearToken(context)
        _currentUser.value = null
        _photos.value = emptyList()
    }
}