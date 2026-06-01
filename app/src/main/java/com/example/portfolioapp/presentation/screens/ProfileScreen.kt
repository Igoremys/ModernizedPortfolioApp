package com.example.portfolioapp.presentation.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.portfolioapp.network.PhotoDto
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.viewModel.AuthViewModel
import com.example.portfolioapp.viewModel.PhotoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettings: () -> Unit,
    onPhotoClick: (PhotoDto) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    photoViewModel: PhotoViewModel = viewModel()
) {
    val context = LocalContext.current

    // Подписываемся на состояния ViewModel
    val currentUser by authViewModel.currentUser.collectAsState()
    val photos by photoViewModel.photos.collectAsState()
    val isLoading by photoViewModel.isLoading.collectAsState()

    // Фильтруем только фото текущего пользователя
    val myPhotos = remember(currentUser, photos) {
        photos.filter { photo ->
            photo.author == currentUser?.email || photo.author == currentUser?.fullName
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header профиля
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarUrl = currentUser?.avatarUrl
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (avatarUrl.isNullOrEmpty()) null else avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .clickable { onSettings() },
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(Color(0xFF27272A)),
                    error = ColorPainter(Color(0xFF3F3F46))
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.fullName ?: "Пользователь",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.description ?: "Нет описания",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                }

                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Заголовок сетки
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Мои фото",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${myPhotos.size}",
                    color = Color(0xFF7C4DFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            // Сетка фото или состояния загрузки/пустоты
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF7C4DFF))
                }
            } else if (myPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Нет опубликованных фото",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Нажмите + в ленте, чтобы добавить первое фото",
                            color = Color(0xFF7C4DFF),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myPhotos, key = { it.id }) { photo ->
                        PhotoGridItem(
                            photo = photo,
                            onClick = { onPhotoClick(photo) }
                        )
                    }
                }
            }
        }
    }
}

// ✅ Отдельный компонент для элемента сетки (можно вынести в components/)
@Composable
private fun PhotoGridItem(
    photo: PhotoDto,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = photo.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFF18181B)),
            error = ColorPainter(Color(0xFF3F3F46))
        )

        // Затемнение внизу для читаемости текста
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xCC000000)
                        )
                    )
                )
        )

        Text(
            text = photo.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}