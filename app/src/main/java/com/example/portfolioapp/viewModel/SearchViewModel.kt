package com.example.portfolioapp.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.network.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    companion object {
        private const val TAG = "SearchViewModel"
    }

    // ==================== STATE ====================
    private val _searchResults = MutableStateFlow<List<UserDto>>(emptyList())
    val searchResults: StateFlow<List<UserDto>> = _searchResults

    private val _selectedUser = MutableStateFlow<UserDto?>(null)
    val selectedUser: StateFlow<UserDto?> = _selectedUser

    private val _userPhotos = MutableStateFlow<List<PhotoDto>>(emptyList())
    val userPhotos: StateFlow<List<PhotoDto>> = _userPhotos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Кэш для дебаунса поиска
    private var lastSearchQuery: String = ""
    private var searchJob: kotlinx.coroutines.Job? = null

    // 🔍 Поиск пользователей с ДЕБАУНСОМ (300ms)
    fun searchUsers(context: Context, keyword: String) {
        // Отменяем предыдущий запрос, если пользователь продолжает печатать
        searchJob?.cancel()

        if (keyword.length < 2) {
            _searchResults.value = emptyList()
            lastSearchQuery = keyword
            return
        }

        // Ждём 300ms после последнего нажатия клавиши
        searchJob = viewModelScope.launch {
            delay(300)
            if (keyword != lastSearchQuery) {
                performSearch(keyword)
                lastSearchQuery = keyword
            }
        }
    }

    private suspend fun performSearch(keyword: String) {
        _isLoading.value = true
        _error.value = null
        try {
            Log.d(TAG, "🔍 Searching: $keyword")
            val response = RetrofitClient.api.searchUsers(keyword)
            if (response.isSuccessful && response.body() != null) {
                _searchResults.value = response.body()!!
                Log.d(TAG, "✅ Found ${response.body()?.size} users")
            } else {
                val errBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Search failed: ${response.code()}, $errBody")
                _error.value = "Ошибка поиска: ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Search exception: ${e.message}", e)
            _error.value = "Нет подключения: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    // 👤 Загрузка публичного профиля + фото (ПАРАЛЛЕЛЬНО)
    fun loadPublicProfile(context: Context, userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedUser.value = null
            _userPhotos.value = emptyList()

            try {
                Log.d(TAG, "👤 Loading profile for userId: $userId")

                // Запускаем оба запроса параллельно
                val profileDeferred = async {
                    RetrofitClient.api.getUserById(userId)
                }
                val photosDeferred = async {
                    RetrofitClient.api.getPhotosByAuthorId(userId)
                }

                val profileResponse = profileDeferred.await()
                val photosResponse = photosDeferred.await()

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    _selectedUser.value = profileResponse.body()

                    if (photosResponse.isSuccessful && photosResponse.body() != null) {
                        _userPhotos.value = photosResponse.body()!!
                        Log.d(TAG, "✅ Loaded profile + ${_userPhotos.value.size} photos")
                    } else {
                        Log.w(TAG, "⚠️ Photos failed, but profile loaded")
                        _userPhotos.value = emptyList()
                    }
                } else {
                    val errBody = profileResponse.errorBody()?.string()
                    Log.e(TAG, "❌ Profile not found: ${profileResponse.code()}, $errBody")
                    _error.value = "Пользователь не найден"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Load profile exception: ${e.message}", e)
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🧹 Сброс состояния при уходе с экрана
    fun clearState() {
        _searchResults.value = emptyList()
        _selectedUser.value = null
        _userPhotos.value = emptyList()
        _error.value = null
        _isLoading.value = false
        searchJob?.cancel()
    }

    fun clearError() {
        _error.value = null
    }
}