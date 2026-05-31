package com.example.portfolioapp.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AuthViewModel : ViewModel() {

    // Состояния загрузки и ошибок
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ✅ Храним данные текущего пользователя
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
                    TokenManager.saveToken(context, authResponse.accessToken, authResponse.user.id)
                    _currentUser.value = authResponse.user
                    println("🔵 [DEBUG] Register success: ${authResponse.user}")
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
                    _currentUser.value = authResponse.user
                    println("🔵 [DEBUG] Login success: ${authResponse.user}")
                    _authSuccess.value = authResponse
                } else {
                    _error.value = "Ошибка входа: ${response.code()}"
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
        println("🔵 [DEBUG] Logout complete")
    }

    // ✅ Загрузка данных пользователя С СЕРВЕРА (при старте приложения)
    fun loadCurrentUser(context: Context) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                if (token == null) {
                    println("🔴 [DEBUG] No token, skipping loadCurrentUser")
                    return@launch
                }

                val response = RetrofitClient.api.getCurrentUser()

                if (response.isSuccessful && response.body() != null) {
                    _currentUser.value = response.body()
                    println("🔵 [DEBUG] Profile loaded: ${response.body()}")
                } else {
                    println("🔴 [DEBUG] Failed to load profile: ${response.code()}")
                }
            } catch (e: Exception) {
                println("🔴 [DEBUG] Exception in loadCurrentUser: ${e.message}")
            }
        }
    }

    // ✅ 🔥 ОБНОВЛЕНИЕ ПРОФИЛЯ НА СЕРВЕРЕ (имя + описание)
    fun updateProfile(context: Context, newFullName: String, newDescription: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val cachedToken = TokenManager.getToken()
                println("🔵 [DEBUG] Token in cache: ${if (cachedToken != null) "present" else "null"}")

                if (cachedToken == null) {
                    _error.value = "Пользователь не авторизован"
                    _isLoading.value = false
                    return@launch
                }

                val request = ProfileUpdateRequest(
                    fullName = newFullName,
                    description = newDescription,
                    avatarUrl = _currentUser.value?.avatarUrl
                )

                println("🔵 [DEBUG] Отправляем запрос: $request")

                val response = RetrofitClient.api.updateProfile(request)

                println("🔵 [DEBUG] Ответ сервера: ${response.code()}, ${response.message()}")

                if (response.isSuccessful && response.body() != null) {
                    println("🔵 [DEBUG] Успех! Новые данные: ${response.body()}")
                    _currentUser.value = response.body()
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("🔴 [DEBUG] Ошибка: ${response.code()}, body: $errorBody")
                    _error.value = "Ошибка обновления: ${response.code()}"
                }
            } catch (e: Exception) {
                println("🔴 [DEBUG] Исключение: ${e.message}")
                e.printStackTrace()
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ 🔥 ЗАГРУЗКА АВАТАРА НА СЕРВЕР (правильный способ!)
    fun uploadAvatar(context: Context, avatarUri: Uri?) {
        if (avatarUri == null) {
            _error.value = "Аватар не выбран"
            return
        }

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

                println("🔵 [UPLOAD] Converting URI to file...")

                // ✅ Конвертируем Uri в File
                val file = uriToFile(context, avatarUri)
                println("🔵 [UPLOAD] File created: ${file.absolutePath}, size: ${file.length()}")

                // ✅ Создаём Multipart запрос
                val requestFile = file.asRequestBody("image/*".toMediaType())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                println("🔵 [UPLOAD] Uploading to server...")

                // ✅ Загружаем на сервер
                val response = RetrofitClient.api.uploadAvatar("Bearer $token", body)

                println("🔵 [UPLOAD] Upload response: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val fileUrl = response.body()!!["url"]!!
                    println("🔵 [UPLOAD] Got URL: $fileUrl")

                    // ✅ Обновляем профиль с новым URL
                    updateProfileWithAvatar(context, fileUrl)
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("🔴 [UPLOAD] Upload failed: ${response.code()}, body: $errorBody")
                    _error.value = "Ошибка загрузки аватара: ${response.code()}"
                }

                // Удаляем временный файл
                if (file.exists()) {
                    file.delete()
                    println("🔵 [UPLOAD] Temp file deleted")
                }
            } catch (e: Exception) {
                println("🔴 [UPLOAD] Exception: ${e.message}")
                e.printStackTrace()
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Вспомогательный метод: Uri → File
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream for $uri")

        val tempFile = File.createTempFile("avatar_${System.currentTimeMillis()}", ".jpg", context.cacheDir)

        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        inputStream.close()
        return tempFile
    }

    // ✅ Обновление профиля с URL аватара
    private suspend fun updateProfileWithAvatar(context: Context, avatarUrl: String) {
        try {
            val token = TokenManager.getToken() ?: return

            val request = ProfileUpdateRequest(
                avatarUrl = avatarUrl,  // ✅ Теперь это HTTP URL!
                fullName = _currentUser.value?.fullName,
                description = _currentUser.value?.description
            )

            println("🔵 [UPDATE] Updating profile with avatar URL: $avatarUrl")

            val response = RetrofitClient.api.updateProfile(request)

            if (response.isSuccessful && response.body() != null) {
                _currentUser.value = response.body()
                println("🔵 [UPDATE] Profile updated successfully: ${response.body()}")
            } else {
                val errorBody = response.errorBody()?.string()
                println("🔴 [UPDATE] Update failed: ${response.code()}, body: $errorBody")
                _error.value = "Ошибка обновления профиля: ${response.code()}"
            }
        } catch (e: Exception) {
            println("🔴 [UPDATE] Exception: ${e.message}")
            _error.value = "Ошибка: ${e.message}"
        }
    }

    // ✅ Получение аватара как Uri (удобный хелпер)
    fun getAvatarUri(): Uri? {
        val avatarUrl = _currentUser.value?.avatarUrl
        return if (avatarUrl.isNullOrEmpty()) null else Uri.parse(avatarUrl)
    }

    // ✅ Получение описания (удобный хелпер)
    fun getDescription(): String? = _currentUser.value?.description

    fun clearError() {
        _error.value = null
    }
}