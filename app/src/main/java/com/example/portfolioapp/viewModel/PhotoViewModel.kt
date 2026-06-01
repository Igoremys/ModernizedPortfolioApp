package com.example.portfolioapp.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class PhotoViewModel : ViewModel() {

    companion object {
        private const val TAG = "PhotoViewModel"
    }

    private val _photos = MutableStateFlow<List<PhotoDto>>(emptyList())
    val photos: StateFlow<List<PhotoDto>> = _photos
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess

    fun loadPhotos(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.api.getAllPhotos()
                if (response.isSuccessful && response.body() != null) {
                    _photos.value = response.body()!!
                    Log.d(TAG, "✅ Loaded ${response.body()?.size} photos")
                } else {
                    val errBody = response.errorBody()?.string()
                    val errorMsg = parseServerError(response.code(), errBody)
                    Log.e(TAG, "❌ Failed: ${response.code()}, $errBody")
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)
                _error.value = "Нет подключения к серверу"
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
                val request = PhotoCreateRequest(title, description, imageUrl)
                val response = RetrofitClient.api.createPhoto(request)
                if (response.isSuccessful && response.body() != null) {
                    val newPhoto = response.body()!!
                    _photos.value = listOf(newPhoto) + _photos.value
                    Log.d(TAG, "✅ Created: ${newPhoto.title}")
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Create failed: ${response.code()}, $errBody")
                    _error.value = "Ошибка: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Create exception: ${e.message}", e)
                _error.value = "Нет подключения"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔥 ГЛАВНОЕ ИСПРАВЛЕНИЕ: корректное формирование полного URL
    private fun buildFullImageUrl(relativeUrl: String): String {
        return when {
            // Уже абсолютный URL
            relativeUrl.startsWith("http") -> relativeUrl
            // Сервер вернул путь с /api/, а BASE_URL тоже заканчивается на /api/ → убираем дубликат
            relativeUrl.startsWith("/api/") -> {
                // BASE_URL = "http://192.168.0.33:8080/api/"
                // relativeUrl = "/api/photos/files/xxx.webp"
                // Результат: "http://192.168.0.33:8080/api/photos/files/xxx.webp"
                RetrofitClient.BASE_URL.removeSuffix("api/") + relativeUrl.removePrefix("/")
            }
            // Обычный относительный путь
            else -> RetrofitClient.BASE_URL + relativeUrl.removePrefix("/")
        }
    }

    fun uploadAndCreatePhoto(context: Context, uri: Uri, title: String, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            var tempFile: File? = null
            try {
                val token = TokenManager.getToken()
                if (token == null) {
                    _error.value = "Пользователь не авторизован"
                    _isLoading.value = false
                    return@launch
                }
                tempFile = uriToFile(context, uri)
                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                val photoApi = RetrofitClient.getPhotoApi()
                val uploadResponse = photoApi.uploadPhoto("Bearer $token", body)
                if (!uploadResponse.isSuccessful || uploadResponse.body()?.containsKey("url") != true) {
                    val err = uploadResponse.errorBody()?.string() ?: "Unknown"
                    Log.e(TAG, "❌ Upload failed: ${uploadResponse.code()}, $err")
                    _error.value = "Ошибка загрузки: ${uploadResponse.code()}"
                    _isLoading.value = false
                    return@launch
                }
                val relativeUrl = uploadResponse.body()!!["url"]!!
                Log.d(TAG, "✅ Got relative URL: $relativeUrl")

                // ✅ ИСПОЛЬЗУЕМ НОВУЮ ФУНКЦИЮ ДЛЯ КОРРЕКТНОГО URL
                val fullUrl = buildFullImageUrl(relativeUrl)
                Log.d(TAG, "✅ Full URL: $fullUrl")

                createPhoto(context, title, description, fullUrl)
                _uploadSuccess.value = true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Upload exception: ${e.message}", e)
                _error.value = "Ошибка: ${e.message}"
            } finally {
                tempFile?.let { if (it.exists()) it.delete() }
                _isLoading.value = false
            }
        }
    }

    fun deletePhoto(context: Context, photoId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.deletePhoto(photoId)
                if (response.isSuccessful) {
                    _photos.value = _photos.value.filter { it.id != photoId }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Delete: ${e.message}")
            }
        }
    }

    // 🔧 Вспомогательные функции
    private fun parseServerError(code: Int, errBody: String?): String {
        return when (code) {
            401 -> "Требуется авторизация"
            403 -> "Доступ запрещён"
            404 -> "Не найдено"
            500 -> {
                try {
                    val json = JSONObject(errBody ?: "{}")
                    "Ошибка сервера: ${json.optString("message", "Internal Error")}"
                } catch (e: Exception) {
                    "Ошибка сервера (500)"
                }
            }
            else -> "Ошибка: $code"
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open $uri")
        val fileName = getFileName(context, uri) ?: "upload_${System.currentTimeMillis()}.jpg"
        val tempFile = File.createTempFile("upload_", fileName, context.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        return tempFile
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) result = it.getString(idx)
                }
            }
        }
        return result ?: uri.lastPathSegment
    }

    fun clearError() { _error.value = null }
    fun clearUploadState() { _uploadSuccess.value = false; _error.value = null }
}