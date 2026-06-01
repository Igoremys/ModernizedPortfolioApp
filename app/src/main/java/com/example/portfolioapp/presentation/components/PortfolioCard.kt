package com.example.portfolioapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.example.portfolioapp.entity.Photo

@Composable
fun ModernPhotoCard(
    photo: Photo,
    onLikeClick: (Photo) -> Unit = {},
    onPhotoClick: (Photo) -> Unit = {}
) {
    var isLiked by remember { mutableStateOf(photo.isLiked) }
    var likesCount by remember { mutableStateOf(photo.likes) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 🖼️ Изображение с placeholder и error
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photo.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable { onPhotoClick(photo) },
                placeholder = ColorPainter(Color(0xFF18181B)),
                error = ColorPainter(Color(0xFF3F3F46))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 👤 Аватар автора
                    if (photo.authorAvatarUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photo.authorAvatarUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Author",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            placeholder = ColorPainter(Color(0xFF7C4DFF)),
                            error = ColorPainter(Color(0xFF7C4DFF))
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF7C4DFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = photo.authorName.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        // ✅ Безопасное отображение имени автора (не email!)
                        Text(
                            text = photo.authorName.takeIf { it.isNotEmpty() && !it.contains("@") }
                                ?: photo.authorName.substringBefore("@"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = photo.description.take(30) + if (photo.description.length > 30) "..." else "",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }

                // ❤️ Лайк с иконками Material
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                isLiked = !isLiked
                                likesCount = if (isLiked) likesCount + 1 else likesCount - 1
                                onLikeClick(photo.copy(isLiked = isLiked, likes = likesCount))
                            },
                            onClickLabel = "Like photo"
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFFF6F91) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = likesCount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}