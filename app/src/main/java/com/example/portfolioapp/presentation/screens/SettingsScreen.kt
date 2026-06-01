package com.example.portfolioapp.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Состояния из AuthViewModel
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val uploadSuccess by authViewModel.avatarUploadSuccess.collectAsState()

    // Локальные состояния для редактирования
    var name by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var description by remember { mutableStateOf(currentUser?.description ?: "") }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(currentUser?.avatarUrl?.let { Uri.parse(it) }) }

    // 🔄 Синхронизация: если данные пришли с сервера — обновляем локальные поля
    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.fullName
            description = it.description ?: ""
            selectedAvatarUri = it.avatarUrl?.let { Uri.parse(it) }
        }
    }

    // 📸 Лаунчер для выбора аватара из галереи
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedAvatarUri = uri
        if (uri != null) {
            authViewModel.uploadAvatar(context, uri)
        }
    }

    // ✅ Реакция на успешную загрузку аватара
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            Toast.makeText(context, "✅ Аватар обновлён", Toast.LENGTH_SHORT).show()
            authViewModel.clearAvatarUploadState()
        }
    }

    // 🧹 Очистка ошибки при изменении полей
    LaunchedEffect(name, description) {
        if (error != null) authViewModel.clearError()
    }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Настройки", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 👤 Аватар с индикатором загрузки
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27272A))
                        .clickable { avatarPicker.launch("image/*") }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(selectedAvatarUri ?: currentUser?.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color(0xFF3F3F46)),
                        error = ColorPainter(Color(0xFF3F3F46))
                    )
                    // Иконка редактирования
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Change avatar",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .background(Color(0xFF7C4DFF), CircleShape)
                            .padding(6.dp)
                    )
                }
                Text(
                    "Нажмите на аватар, чтобы изменить",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(Modifier.height(32.dp))

                // ✏️ Поле "Имя"
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // 📝 Поле "О себе"
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("О себе", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(16.dp))

                // ❌ Ошибка
                error?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFF5252).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "❌ $msg",
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 💾 Кнопка "Сохранить изменения"
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            authViewModel.updateProfile(context, name, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                    enabled = !isLoading && name.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Сохранить изменения", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 🚪 Кнопка "Выйти"
                OutlinedButton(
                    onClick = {
                        authViewModel.logout(context)
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text("Выйти из аккаунта", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}