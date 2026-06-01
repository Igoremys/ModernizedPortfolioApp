package com.example.portfolioapp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.example.portfolioapp.network.UserDto
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.viewModel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onUserSelected: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Запуск поиска (дебаунс реализован внутри SearchViewModel)
    LaunchedEffect(query) {
        viewModel.searchUsers(context, query)
    }

    // Очистка состояния при выходе с экрана
    DisposableEffect(Unit) {
        onDispose { viewModel.clearState() }
    }

    GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Поиск пользователей", color = Color.White, fontWeight = FontWeight.Bold) },
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Введите имя или email...", color = Color.LightGray) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.LightGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF7C4DFF),
                        unfocusedLabelColor = Color.LightGray,
                        cursorColor = Color(0xFF7C4DFF)
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    }
                } else {
                    error?.let { msg ->
                        Text(
                            text = "❌ $msg",
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 14.sp
                        )
                    }

                    if (results.isEmpty() && query.length >= 2) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Ничего не найдено", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else if (query.length < 2) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Введите минимум 2 символа", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(results, key = { it.id }) { user ->
                                UserSearchItem(user = user, onClick = { onUserSelected(user.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchItem(user: UserDto, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(if (user.avatarUrl.isNullOrEmpty()) null else user.avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar of ${user.fullName}",
            modifier = Modifier.size(50.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFF27272A)),
            error = ColorPainter(Color(0xFF3F3F46))
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                user.fullName,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(user.email, fontSize = 14.sp, color = Color.LightGray)
        }
    }
}