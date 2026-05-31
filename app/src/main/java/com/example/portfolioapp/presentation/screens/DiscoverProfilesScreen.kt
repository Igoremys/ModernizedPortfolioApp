package com.example.portfolioapp.presentation.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.portfolioapp.entity.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverProfilesScreen(
    profiles: List<UserProfile>,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProfiles = profiles.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.bio.contains(searchQuery, ignoreCase = true)
    }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Discover", color = Color.White, fontWeight = FontWeight.Bold) },
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
                    .padding(16.dp)
            ) {
                // Поиск
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search profiles...", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = Color.LightGray)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.LightGray,
                        cursorColor = Color(0xFF7C4DFF)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(20.dp))

                // Список профилей
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProfiles) { profile ->
                        ProfileCard(profile = profile)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (profile.avatarUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.avatarUri)
                        .build(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C4DFF))
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    profile.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    profile.bio,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Локальная функция градиента
@Composable
fun GradientBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF09090B),
                        Color(0xFF18181B),
                        Color(0xFF111827)
                    )
                )
            ),
        content = content
    )
}