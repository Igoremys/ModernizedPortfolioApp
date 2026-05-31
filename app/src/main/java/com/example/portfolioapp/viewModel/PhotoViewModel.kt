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

    fun loadPhotos(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val token = TokenManager.getToken(context) ?: run {
                    _error.value = "Пользователь не авторизован"
                    _isLoading.value = false
                    return@launch
                }

                val response = RetrofitClient.api.getAllPhotos(token)

                if (response.isSuccessful && response.body() != null) {
                    _photos.value = response.body()!!
                } else {
                    _error.value = "Ошибка загрузки: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPhoto(context: Context, title: String, description: String, imageUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val token = TokenManager.getToken(context) ?: run {
                    _error.value = "Пользователь не авторизован"
                    _isLoading.value = false
                    return@launch
                }

                val request = PhotoCreateRequest(title, description, imageUrl)
                val response = RetrofitClient.api.createPhoto(request, token)

                if (response.isSuccessful && response.body() != null) {
                    val currentList = _photos.value.toMutableList()
                    currentList.add(0, response.body()!!)
                    _photos.value = currentList
                } else {
                    _error.value = "Ошибка создания: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePhoto(context: Context, photoId: Long) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(context) ?: return@launch

                val response = RetrofitClient.api.deletePhoto(photoId, token)

                if (response.isSuccessful) {
                    val currentList = _photos.value.toMutableList()
                    currentList.removeAll { it.id == photoId }
                    _photos.value = currentList
                }
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            }
        }
    }
}