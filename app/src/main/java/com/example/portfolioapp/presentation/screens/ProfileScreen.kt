package com.example.portfolioapp.presentation.screens

import android.net.Uri
import androidx.compose.foundation.background  // ✅ Добавлено: важный импорт!
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.presentation.components.GradientBackground

@Composable
fun ProfileScreen(
    userName: String,
    avatarUri: Uri?,
    description: String,
    myPhotos: List<Photo>,
    onSettings: () -> Unit
) {
    GradientBackground {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Шапка профиля
            item {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUri)
                                .build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        // ✅ Теперь background доступен
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C4DFF)),  // ✅ background работает
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.toString() ?: "U",
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(userName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(description, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = onSettings,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                    ) {
                        Text("Edit profile", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Заголовок раздела
            item {
                Text(
                    "My Photos",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 12.dp)
                )
            }

            // Сетка фото пользователя
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((myPhotos.size.coerceAtLeast(1) / 2 * 160 + 120).dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(myPhotos) { photo ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photo.uri)
                                .build(),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(30.dp)) }
        }
    }
}