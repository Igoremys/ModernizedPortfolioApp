package com.example.portfolioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.presentation.screens.*
import com.example.portfolioapp.viewModel.AuthViewModel
import com.example.portfolioapp.viewModel.PhotoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF7C4DFF),
                    secondary = Color(0xFFFF6F91),
                    surface = Color(0xFF18181B),
                    background = Color(0xFF09090B)
                )
            ) {
                PortfolioApp()
            }
        }
    }
}

@Composable
fun PortfolioApp(
    authViewModel: AuthViewModel = viewModel(),
    photoViewModel: PhotoViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Состояния из ViewModel
    val currentUser by authViewModel.currentUser.collectAsState()
    val photos by photoViewModel.photos.collectAsState()
    val isLoading by photoViewModel.isLoading.collectAsState()
    val uploadSuccess by photoViewModel.uploadSuccess.collectAsState()

    // Загрузка данных при старте
    LaunchedEffect(Unit) {
        val token = com.example.portfolioapp.network.TokenManager.getToken()
        if (token != null) {
            authViewModel.loadCurrentUser(context)
            photoViewModel.loadPhotos(context)
        }
    }

    // Реакция на успешную загрузку фото
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            photoViewModel.clearUploadState()
            photoViewModel.loadPhotos(context) // Обновляем ленту
        }
    }

    GradientBackground {
        NavHost(navController = navController, startDestination = "login") {

            // 🔐 LOGIN
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        photoViewModel.loadPhotos(context)
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    viewModel = authViewModel
                )
            }

            // 📝 REGISTER
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("home") { popUpTo("register") { inclusive = true } }
                    },
                    onBackToLogin = { navController.popBackStack() },
                    viewModel = authViewModel
                )
            }

            // 🏠 HOME
            composable("home") {
                HomeScreen(
                    viewModel = photoViewModel,
                    onPhotoClick = { photo -> navController.navigate("photo_detail/${photo.id}") },
                    onLikeClick = { /* TODO: Add like logic */ },
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToDiscover = { navController.navigate("discover") },
                    onNavigateToAddPhoto = { navController.navigate("add") }
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    }
                }
            }

            // 👤 PROFILE
            composable("profile") {
                ProfileScreen(
                    onSettings = { navController.navigate("settings") },
                    onPhotoClick = { photo -> navController.navigate("photo_detail/${photo.id}") },
                    authViewModel = authViewModel,
                    photoViewModel = photoViewModel
                )
            }

            // ⚙️ SETTINGS
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        authViewModel.logout(context)
                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                    },
                    authViewModel = authViewModel
                )
            }

            // 🔍 SEARCH
            composable("search") {
                val searchViewModel: com.example.portfolioapp.viewModel.SearchViewModel = viewModel()
                SearchScreen(
                    onUserSelected = { userId -> navController.navigate("public_profile/$userId") },
                    onBack = { navController.popBackStack() },
                    viewModel = searchViewModel
                )
            }

            // 🔍 DISCOVER
            composable("discover") {
                DiscoverProfilesScreen(
                    profiles = listOf(), // TODO: Подтянуть из SearchViewModel или API
                    onBack = { navController.popBackStack() }
                )
            }

            // ➕ ADD PHOTO
            composable("add") {
                AddPhotoScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = photoViewModel
                )
            }

            // 🖼️ PHOTO DETAIL
            composable(
                route = "photo_detail/{photoId}",
                arguments = listOf(navArgument("photoId") { type = NavType.IntType })
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getInt("photoId")
                val photo = photos.firstOrNull { it.id.toInt() == photoId }
                if (photo != null) {
                    PhotoDetailScreen(
                        photo = photo.toPhotoEntity(),
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    navController.popBackStack()
                }
            }

            // 👤 PUBLIC PROFILE
            composable(
                route = "public_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
                val searchViewModel: com.example.portfolioapp.viewModel.SearchViewModel = viewModel()
                val user by searchViewModel.selectedUser.collectAsState()
                val userPhotos by searchViewModel.userPhotos.collectAsState()
                val isSearching by searchViewModel.isLoading.collectAsState()

                LaunchedEffect(userId) {
                    searchViewModel.loadPublicProfile(context, userId)
                }

                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    }
                } else if (user != null) {
                    PublicProfileScreen(
                        user = user!!,
                        photos = userPhotos,
                        onPhotoClick = { /* Можно добавить навигацию */ },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

// ============================================
// 🔄 Маппер: DTO → Entity (для совместимости с PhotoDetailScreen)
// ============================================
private fun com.example.portfolioapp.network.PhotoDto.toPhotoEntity() =
    com.example.portfolioapp.entity.Photo(
        id = this.id.toInt(),
        title = this.title,
        description = this.description,
        uri = this.imageUrl.takeIf { it.isNotEmpty() }?.let { android.net.Uri.parse(it) },
        likes = 0,
        isLiked = false,
        authorName = this.author,
        authorAvatarUri = null
    )