package com.example.portfolioapp.presentation.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.network.PhotoDto
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.presentation.components.ModernPhotoCard
import com.example.portfolioapp.viewModel.PhotoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PhotoViewModel = viewModel(),
    onPhotoClick: (Photo) -> Unit = {},
    onLikeClick: (Photo) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDiscover: () -> Unit = {},
    onNavigateToAddPhoto: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val photosDto by viewModel.photos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // ✅ Безопасный маппинг с проверкой на null/empty
    val photos = photosDto.mapNotNull { it.toPhotoEntity() }

    LaunchedEffect(Unit) {
        viewModel.loadPhotos(context)
    }

    GradientBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToAddPhoto,
                    containerColor = Color(0xFF7C4DFF)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Photo", tint = Color.White)
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF18181B)) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, null, tint = if (selectedTab == 0) Color(0xFF7C4DFF) else Color.Gray) },
                        label = { Text("Home", color = if (selectedTab == 0) Color(0xFF7C4DFF) else Color.Gray) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; onNavigateToDiscover() },
                        icon = { Icon(Icons.Default.Search, null, tint = if (selectedTab == 1) Color(0xFF7C4DFF) else Color.Gray) },
                        label = { Text("Discover", color = if (selectedTab == 1) Color(0xFF7C4DFF) else Color.Gray) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; onNavigateToProfile() },
                        icon = { Icon(Icons.Default.Person, null, tint = if (selectedTab == 2) Color(0xFF7C4DFF) else Color.Gray) },
                        label = { Text("Profile", color = if (selectedTab == 2) Color(0xFF7C4DFF) else Color.Gray) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    }
                }

                error?.let { msg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        color = Color(0xFFFF5252).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "❌ $msg", color = Color(0xFFFF5252), modifier = Modifier.padding(16.dp), fontSize = 14.sp)
                    }
                }

                if (!isLoading && error == null && photos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Нет фото", color = Color.Gray, fontSize = 16.sp)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        ModernPhotoCard(
                            photo = photo,
                            onLikeClick = onLikeClick,
                            onPhotoClick = onPhotoClick
                        )
                    }
                }
            }
        }
    }
}

// ✅ Безопасный маппер: возвращает null, если данные невалидны
private fun PhotoDto.toPhotoEntity(): Photo? {
    // Если imageUrl пустой или null — пропускаем это фото
    if (imageUrl.isBlank()) return null

    return Photo(
        id = this.id.toInt(),
        title = this.title.takeIf { it.isNotBlank() } ?: "Без названия",
        description = this.description.takeIf { it.isNotBlank() } ?: "",
        uri = Uri.parse(imageUrl),
        likes = 0,
        isLiked = false,
        // ✅ Если author содержит @ (email), берём часть до @ или используем fullName из UserDto, если будет передан
        authorName = this.author.takeIf { it.isNotBlank() && !it.contains("@") }
            ?: this.author.substringBefore("@", "Пользователь"),
        authorAvatarUri = null
    )
}