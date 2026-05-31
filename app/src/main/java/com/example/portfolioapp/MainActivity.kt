package com.example.portfolioapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.presentation.screens.AddPhotoScreen
import com.example.portfolioapp.presentation.screens.DiscoverProfilesScreen
import com.example.portfolioapp.presentation.screens.HomeScreen
import com.example.portfolioapp.presentation.screens.LoginScreen
import com.example.portfolioapp.presentation.screens.PhotoDetailScreen
import com.example.portfolioapp.presentation.screens.ProfileScreen
import com.example.portfolioapp.presentation.screens.SettingsScreen

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
fun PortfolioApp() {
    val navController = rememberNavController()

    var userName by remember { mutableStateOf("Igor Photographer") }
    var userDescription by remember { mutableStateOf("Travel photographer & visual storyteller") }
    var userAvatarUri by remember { mutableStateOf<Uri?>(null) }

    val photos = remember {
        mutableStateListOf<Photo>().apply {
            addAll(
                listOf(
                    Photo(1, "Mountain View", "Beautiful sunrise over the Alps", Uri.parse("https://picsum.photos/seed/mountain/800/600"), 124, false, "Igor", Uri.parse("https://picsum.photos/seed/igor/100/100")),
                    Photo(2, "Urban Night", "City lights and architecture", Uri.parse("https://picsum.photos/seed/urban/800/600"), 89, true, "Igor", Uri.parse("https://picsum.photos/seed/igor/100/100")),
                    Photo(3, "Ocean Breeze", "Coastal landscape at sunset", Uri.parse("https://picsum.photos/seed/ocean/800/600"), 210, false, "Anna", Uri.parse("https://picsum.photos/seed/anna/100/100")),
                    Photo(4, "Forest Path", "Misty morning in the woods", Uri.parse("https://picsum.photos/seed/forest/800/600"), 56, false, "Max", Uri.parse("https://picsum.photos/seed/max/100/100"))
                )
            )
        }
    }

    val onLikeClick: (Photo) -> Unit = { updated ->
        val idx = photos.indexOfFirst { it.id == updated.id }
        if (idx != -1) photos[idx] = updated
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLogin = { navController.navigate("home") },
                onRegister = { navController.navigate("home") }
            )
        }
        composable("home") {
            HomeScreen(
                photos = photos.toList(),
                onPhotoClick = { navController.navigate("photo_detail/${it.id}") },
                onLikeClick = onLikeClick,
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToDiscover = { navController.navigate("discover") },
                onNavigateToAddPhoto = { navController.navigate("add") }
            )
        }
        composable("profile") {
            ProfileScreen(
                userName = userName,
                avatarUri = userAvatarUri,
                description = userDescription,
                myPhotos = photos.filter { it.authorName == userName },
                onSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                currentName = userName,
                currentAvatarUri = userAvatarUri,
                currentDescription = userDescription,
                onNameChange = { userName = it },
                onDescriptionChange = { userDescription = it },
                onAvatarChange = { userAvatarUri = it },
                onBack = { navController.popBackStack() }
            )
        }
        composable("discover") {
            DiscoverProfilesScreen(
                profiles = listOf(
                    com.example.portfolioapp.entity.UserProfile(1, "Anna Visuals", "Street photography", Uri.parse("https://picsum.photos/seed/anna/100/100")),
                    com.example.portfolioapp.entity.UserProfile(2, "Max Travel", "Landscape creator", Uri.parse("https://picsum.photos/seed/max/100/100"))
                ),
                onBack = { navController.popBackStack() }
            )
        }
        composable("add") {
            AddPhotoScreen(onSave = { title, desc, uri ->
                photos.add(0, Photo(photos.size + 1, title, desc, uri, 0, false, userName, userAvatarUri))
                navController.popBackStack()
            })
        }
        composable(
            route = "photo_detail/{photoId}",
            arguments = listOf(navArgument("photoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getInt("photoId")
            val photo = photos.find { it.id == photoId }
            if (photo != null) {
                PhotoDetailScreen(photo = photo, onBack = { navController.popBackStack() })
            } else {
                navController.popBackStack()
            }
        }
    }
}