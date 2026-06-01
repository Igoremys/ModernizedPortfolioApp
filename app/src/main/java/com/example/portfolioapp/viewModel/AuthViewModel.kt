package com.example.portfolioapp.viewModel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import java.io.File

class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    // ==================== STATE ====================
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser

    private val _authSuccess = MutableStateFlow<AuthResponse?>(null)
    val authSuccess: StateFlow<AuthResponse?> = _authSuccess

    private val _avatarUploadSuccess = MutableStateFlow(false)
    val avatarUploadSuccess: StateFlow<Boolean> = _avatarUploadSuccess

    // ==================== AUTH ====================
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
                    Log.d(TAG, "✅ Register success: ${authResponse.user}")
                    _authSuccess.value = authResponse
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Register failed: ${response.code()}, $errBody")
                    _error.value = "Ошибка регистрации: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Register exception: ${e.message}", e)
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                    Log.d(TAG, "✅ Login success: ${authResponse.user}")
                    _authSuccess.value = authResponse
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Login failed: ${response.code()}, $errBody")
                    _error.value = "Ошибка входа: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Login exception: ${e.message}", e)
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(context: Context) {
        TokenManager.clearToken(context)
        _currentUser.value = null
        _authSuccess.value = null
        _avatarUploadSuccess.value = false
        Log.d(TAG, "✅ Logout complete")
    }

    fun loadCurrentUser(context: Context) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                if (token == null) {
                    Log.w(TAG, "⚠️ No token, skipping loadCurrentUser")
                    return@launch
                }
                val response = RetrofitClient.api.getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    _currentUser.value = response.body()
                    Log.d(TAG, "✅ Profile loaded: ${response.body()}")
                } else {
                    Log.e(TAG, "❌ Failed to load profile: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception in loadCurrentUser: ${e.message}", e)
            }
        }
    }

    // ==================== PROFILE UPDATE ====================
    fun updateProfile(context: Context, newFullName: String, newDescription: String = "") {
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

                val request = ProfileUpdateRequest(
                    fullName = newFullName,
                    description = newDescription,
                    avatarUrl = _currentUser.value?.avatarUrl
                )

                val response = RetrofitClient.api.updateProfile(request)

                if (response.isSuccessful && response.body() != null) {
                    _currentUser.value = response.body()
                    Log.d(TAG, "✅ Profile updated: ${response.body()}")
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Update failed: ${response.code()}, $errBody")
                    _error.value = "Ошибка обновления: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Update exception: ${e.message}", e)
                _error.value = "Нет подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== AVATAR UPLOAD ====================
    fun uploadAvatar(context: Context, avatarUri: Uri?) {
        if (avatarUri == null) {
            _error.value = "Аватар не выбран"
            return
        }

        val mimeType = context.contentResolver.getType(avatarUri)
        if (mimeType == null || !mimeType.startsWith("image/")) {
            _error.value = "Разрешены только изображения (JPEG, PNG, WebP)"
            return
        }

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

                Log.d(TAG, "📤 Converting URI to file...")
                tempFile = uriToFile(context, avatarUri)
                Log.d(TAG, "📤 File: ${tempFile.name}, size: ${tempFile.length()}")

                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                Log.d(TAG, "📤 Uploading avatar...")
                val response = RetrofitClient.api.uploadAvatar("Bearer $token", body)

                Log.d(TAG, "📤 Upload response: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()!!
                    val relativeUrl = responseData["url"] ?: run {
                        Log.e(TAG, "❌ No 'url' field in response: $responseData")
                        _error.value = "Сервер не вернул URL файла"
                        return@launch
                    }
                    Log.d(TAG, "✅ Got relative URL: $relativeUrl")

                    val fullUrl = if (relativeUrl.startsWith("/api")) {
                        RetrofitClient.BASE_URL.removeSuffix("/") + relativeUrl
                    } else {
                        RetrofitClient.BASE_URL + relativeUrl.removePrefix("/")
                    }
                    Log.d(TAG, "✅ Full avatar URL: $fullUrl")

                    updateProfileWithAvatar(context, fullUrl)
                    _avatarUploadSuccess.value = true
                } else {
                    val errBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Upload failed: ${response.code()}, body: $errBody")
                    _error.value = "Ошибка загрузки: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Upload exception: ${e.message}", e)
                _error.value = "Ошибка: ${e.message}"
            } finally {
                tempFile?.let {
                    if (it.exists()) {
                        it.delete()
                        Log.d(TAG, "🗑️ Temp file deleted")
                    }
                }
                _isLoading.value = false
            }
        }
    }

    // ==================== HELPERS ====================
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream for $uri")

        val fileName = getFileName(context, uri) ?: "avatar_${System.currentTimeMillis()}.jpg"
        val tempFile = File.createTempFile("upload_", fileName, context.cacheDir)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) result = it.getString(idx)
                }
            }
        }
        if (result == null) result = uri.lastPathSegment
        return result
    }

    private suspend fun updateProfileWithAvatar(context: Context, avatarUrl: String) {
        try {
            val token = TokenManager.getToken() ?: return

            val request = ProfileUpdateRequest(
                avatarUrl = avatarUrl,
                fullName = _currentUser.value?.fullName,
                description = _currentUser.value?.description
            )

            Log.d(TAG, "🔄 Updating profile with avatar: $avatarUrl")
            val response = RetrofitClient.api.updateProfile(request)

            if (response.isSuccessful && response.body() != null) {
                _currentUser.value = response.body()
                Log.d(TAG, "✅ Profile + avatar updated successfully")
            } else {
                val errBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Profile update failed: ${response.code()}, $errBody")
                _error.value = "Ошибка обновления профиля: ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Profile update exception: ${e.message}", e)
            _error.value = "Ошибка: ${e.message}"
        }
    }

    // ==================== PUBLIC METHODS ====================
    fun getAvatarUri(): Uri? {
        val avatarUrl = _currentUser.value?.avatarUrl
        return if (avatarUrl.isNullOrEmpty()) null else Uri.parse(avatarUrl)
    }

    fun getDescription(): String? = _currentUser.value?.description

    fun clearError() {
        _error.value = null
    }

    fun clearAuthState() {
        _authSuccess.value = null
        _error.value = null
    }

    fun clearAvatarUploadState() {
        _avatarUploadSuccess.value = false
        _error.value = null
    }
}