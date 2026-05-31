package com.example.portfolioapp.presentation.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.presentation.components.GradientBackground  // ✅ Добавлен импорт
import com.example.portfolioapp.presentation.components.ModernPhotoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    photos: List<Photo>,
    onPhotoClick: (Photo) -> Unit = {},
    onLikeClick: (Photo) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDiscover: () -> Unit = {},
    onNavigateToAddPhoto: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    GradientBackground {  // ✅ Теперь используется функция из components/
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
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF7C4DFF), selectedTextColor = Color(0xFF7C4DFF),
                            unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; onNavigateToDiscover() },
                        icon = { Icon(Icons.Default.Search, null) },
                        label = { Text("Discover") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF7C4DFF), selectedTextColor = Color(0xFF7C4DFF),
                            unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; onNavigateToProfile() },
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF7C4DFF), selectedTextColor = Color(0xFF7C4DFF),
                            unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp)
            ) {
                items(photos) { photo ->
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