package com.example.portfolioapp.presentation.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.portfolioapp.network.PhotoDto
import com.example.portfolioapp.network.UserDto
import com.example.portfolioapp.presentation.components.GradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    user: UserDto,
    photos: List<PhotoDto>,
    onPhotoClick: (PhotoDto) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Профиль", color = Color.White, fontWeight = FontWeight.Bold) },
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
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(if (user.avatarUrl.isNullOrEmpty()) null else user.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar of ${user.fullName}",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color(0xFF27272A)),
                        error = ColorPainter(Color(0xFF3F3F46))
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = user.fullName,
                        fontSize = 22.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.description ?: "Нет описания",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Divider(color = Color.Gray.copy(alpha = 0.3f))

                Text(
                    text = "Фотографии (${photos.size})",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 16.sp
                )

                if (photos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Нет опубликованных фото", color = Color.Gray, fontSize = 16.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photo.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = photo.title,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { onPhotoClick(photo) },
                                contentScale = ContentScale.Crop,
                                placeholder = ColorPainter(Color(0xFF18181B)),
                                error = ColorPainter(Color(0xFF3F3F46))
                            )
                        }
                    }
                }
            }
        }
    }
}