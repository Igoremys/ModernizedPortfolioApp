package com.example.portfolioapp.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhotoViewModel : ViewModel() {

    private val _photos = MutableStateFlow<List<PhotoDto>>(emptyList())
    val photos: StateFlow<List<PhotoDto>> = _photos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 🔥 Загрузка всех фото с сервера
    fun loadPhotos(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // ✅ ПРОВЕРКА: токен есть в кэше (для отладки)
                val token = TokenManager.getToken()
                if (token == null) {
                    _error.value = "Пользователь не авторизован"
                    _isLoading.value = false
                    return@launch
                }

                // ✅ БЕЗ токена — интерцептор добавит его сам!
                val response = RetrofitClient.api.getAllPhotos()

                if (response.isSuccessful && response.body() != null) {
                    _photos.value = response.body()!!
                    println("🔵 [DEBUG] Загружено фото: ${response.body()?.size}")
                } else {
                    println("🔴 [DEBUG] Ошибка загрузки фото: ${response.code()}")
                    _error.value = "Ошибка загрузки: ${response.code()}"
                }
            } catch (e: Exception) {
                println("🔴 [DEBUG] Исключение: ${e.message}")
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 Создание нового фото на сервере
    fun createPhoto(context: Context, title: String, description: String, imageUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val token = TokenManager.getToken()
                if (token == null) {
                    _error.value = "Пользователь не авторизован"
                    _isLoading.value = false
                    return@launch
                }

                val request = PhotoCreateRequest(title, description, imageUrl)

                // ✅ БЕЗ токена — интерцептор добавит его сам!
                val response = RetrofitClient.api.createPhoto(request)

                if (response.isSuccessful && response.body() != null) {
                    // Добавляем новое фото в список
                    val currentList = _photos.value.toMutableList()
                    currentList.add(0, response.body()!!)
                    _photos.value = currentList
                    println("🔵 [DEBUG] Фото создано: ${response.body()?.title}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("🔴 [DEBUG] Ошибка создания: ${response.code()}, body: $errorBody")
                    _error.value = "Ошибка создания: ${response.code()}"
                }
            } catch (e: Exception) {
                println("🔴 [DEBUG] Исключение: ${e.message}")
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 Удаление фото с сервера
    fun deletePhoto(context: Context, photoId: Long) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                if (token == null) return@launch

                // ✅ БЕЗ токена — интерцептор добавит его сам!
                val response = RetrofitClient.api.deletePhoto(photoId)

                if (response.isSuccessful) {
                    // Удаляем из списка
                    val currentList = _photos.value.toMutableList()
                    currentList.removeAll { it.id == photoId }
                    _photos.value = currentList
                    println("🔵 [DEBUG] Фото удалено: $photoId")
                }
            } catch (e: Exception) {
                println("🔴 [DEBUG] Ошибка удаления: ${e.message}")
                _error.value = "Ошибка удаления: ${e.message}"
            }
        }
    }
}