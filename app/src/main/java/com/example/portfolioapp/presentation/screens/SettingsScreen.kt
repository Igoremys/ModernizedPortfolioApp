package com.example.portfolioapp.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.portfolioapp.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentName: String,
    currentAvatarUri: Uri?,
    currentDescription: String,
    onNameChange: (String) -> Unit,      // Для локального обновления (можно оставить)
    onDescriptionChange: (String) -> Unit, // Для локального обновления (можно оставить)
    onAvatarChange: (Uri?) -> Unit,       // Для локального обновления (можно оставить)
    onBack: () -> Unit,
    onLogout: () -> Unit,

    // ✅ НОВЫЙ параметр: сохранение ВСЕХ изменений одним запросом
    onSave: () -> Unit,

    authViewModel: AuthViewModel = viewModel()
) {
    // ✅ Локальные состояния для редактирования
    var name by remember { mutableStateOf(currentName) }
    var description by remember { mutableStateOf(currentDescription) }
    var avatarUri by remember { mutableStateOf(currentAvatarUri) }

    val context = LocalContext.current

    // ✅ СИНХРОНИЗАЦИЯ: если пришли новые данные из ViewModel — обновляем локальные состояния
    LaunchedEffect(currentName) {
        name = currentName
    }
    LaunchedEffect(currentDescription) {
        description = currentDescription
    }
    LaunchedEffect(currentAvatarUri) {
        avatarUri = currentAvatarUri
    }

    // 📸 Лаунчер для выбора фото
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
        onAvatarChange(uri)  // Обновляем локальное состояние
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "Settings",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(32.dp))

            // 👤 Avatar
            AsyncImage(
                model = avatarUri,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { imagePicker.launch("image/*") },
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(Color(0xFF27272A)),
                error = androidx.compose.ui.graphics.painter.ColorPainter(Color(0xFF3F3F46))
            )
            TextButton(onClick = { imagePicker.launch("image/*") }) {
                Text("Change Avatar", color = Color(0xFF7C4DFF))
            }
            Spacer(Modifier.height(32.dp))

            // ✏️ Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    onNameChange(it)  // Для локального кэша в MainActivity
                },
                label = { Text("Display Name", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(16.dp))

            // 📝 Description
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    onDescriptionChange(it)  // Для локального кэша в MainActivity
                },
                label = { Text("Bio", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(32.dp))

            // 💾 Save button — ✅ ИСПРАВЛЕНО: один вызов onSave()
            Button(
                onClick = {
                    // ✅ Вызываем ЕДИНЫЙ колбэк для сохранения ВСЕХ изменений
                    onSave()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(16.dp))

            // 🚪 Logout
            OutlinedButton(
                onClick = {
                    authViewModel.logout(context)
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
            ) {
                Text("Logout", fontWeight = FontWeight.Bold)
            }
        }
    }
}