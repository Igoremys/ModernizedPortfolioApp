package com.example.portfolioapp.presentation.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.viewModel.PhotoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoScreen(
    onBack: () -> Unit,
    viewModel: PhotoViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val uploadSuccess by viewModel.uploadSuccess.collectAsState()

    // 📸 Лаунчер для выбора изображения из галереи
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // 🔄 Реакция на успешную загрузку
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            Toast.makeText(context, "✅ Фото успешно загружено!", Toast.LENGTH_SHORT).show()
            viewModel.clearUploadState()
            onBack()
        }
    }

    // 🧹 Очистка ошибки при изменении полей
    LaunchedEffect(title, description, selectedImageUri) {
        if (error != null) viewModel.clearError()
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔙 Кнопка "Назад"
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
                    text = "Add New Photo",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ❌ Отображение ошибки
            error?.let { msg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFF5252).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "❌ $msg",
                        color = Color(0xFFFF5252),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // 🖼️ Область выбора/предпросмотра изображения
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2A2A))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF7C4DFF), Color(0xFFFF6F91))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected photo",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add photo",
                            tint = Color(0xFF7C4DFF),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap to select photo",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 📝 Поле "Название"
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF7C4DFF),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFF7C4DFF)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // 📝 Поле "Описание"
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF7C4DFF),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFF7C4DFF)
                )
            )

            Spacer(Modifier.weight(1f))

            // 🚀 Кнопка загрузки
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedImageUri != null) {
                        viewModel.uploadAndCreatePhoto(context, selectedImageUri!!, title, description)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C4DFF)
                ),
                enabled = !isLoading && title.isNotBlank() && selectedImageUri != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("📤 Upload Photo", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}