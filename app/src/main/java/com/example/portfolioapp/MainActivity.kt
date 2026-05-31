package com.example.portfolioapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.portfolioapp.entity.Photo
import com.example.portfolioapp.presentation.screens.*
import com.example.portfolioapp.viewModel.AuthViewModel
import com.example.portfolioapp.viewModel.PhotoViewModel
import com.example.portfolioapp.network.TokenManager

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
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ✅ Текущий пользователь из AuthViewModel
    val currentUser by authViewModel.currentUser.collectAsState()

    // ✅ Локальные переменные для редактирования (кэш)
    var userName by remember { mutableStateOf("Igor Photographer") }
    var userDescription by remember { mutableStateOf("Travel photographer & visual storyteller") }
    var userAvatarUri by remember { mutableStateOf<Uri?>(null) }

    // ✅ Синхронизация: когда данные пришли с сервера — обновляем локальный кэш
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            userName = user.fullName
            userDescription = user.description ?: userDescription
            userAvatarUri = if (user.avatarUrl.isNullOrEmpty()) null else Uri.parse(user.avatarUrl)
            println("🔵 [UI] Synced: name=$userName, desc=$userDescription, avatar=${user.avatarUrl}")
        }
    }

    // ✅ Тестовые фото (заглушка)
    val testPhotos = remember {
        mutableStateListOf<Photo>().apply {
            addAll(
                listOf(
                    Photo(id = 1, title = "Mountain View", description = "Beautiful sunrise over the Alps", uri = Uri.parse("https://picsum.photos/seed/mountain/800/600"), likes = 124, isLiked = false, authorName = "Igor", authorAvatarUri = Uri.parse("https://picsum.photos/seed/igor/100/100")),
                    Photo(id = 2, title = "Urban Night", description = "City lights and architecture", uri = Uri.parse("https://picsum.photos/seed/urban/800/600"), likes = 89, isLiked = true, authorName = "Igor", authorAvatarUri = Uri.parse("https://picsum.photos/seed/igor/100/100")),
                    Photo(id = 3, title = "Ocean Breeze", description = "Coastal landscape at sunset", uri = Uri.parse("https://picsum.photos/seed/ocean/800/600"), likes = 210, isLiked = false, authorName = "Anna", authorAvatarUri = Uri.parse("https://picsum.photos/seed/anna/100/100")),
                    Photo(id = 4, title = "Forest Path", description = "Misty morning in the woods", uri = Uri.parse("https://picsum.photos/seed/forest/800/600"), likes = 56, isLiked = false, authorName = "Max", authorAvatarUri = Uri.parse("https://picsum.photos/seed/max/100/100"))
                )
            )
        }
    }

    // ✅ Глобальный список всех фото
    val allPhotos = remember { mutableStateListOf<Photo>() }
    val onLikeClick: (Photo) -> Unit = { updated ->
        val idx = allPhotos.indexOfFirst { it.id == updated.id }
        if (idx != -1) allPhotos[idx] = updated
    }

    // ✅ Загружаем профиль при старте, если токен есть
    LaunchedEffect(Unit) {
        val token = TokenManager.getToken()
        println("🔵 [STARTUP] Token: ${if (token != null) "OK" else "NULL"}")
        if (token != null) {
            authViewModel.loadCurrentUser(context)
        }
    }

    NavHost(navController = navController, startDestination = "login") {

        // 🔐 LOGIN
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onNavigateToRegister = { navController.navigate("register") },
                viewModel = authViewModel
            )
        }

        // 📝 REGISTER
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("home") },
                onBackToLogin = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        // 🏠 HOME
        composable("home") {
            val photoViewModel: PhotoViewModel = viewModel()
            val apiPhotos by photoViewModel.photos.collectAsState()
            val localContext = LocalContext.current

            LaunchedEffect(Unit) {
                photoViewModel.loadPhotos(localContext)
            }

            LaunchedEffect(apiPhotos, currentUser) {
                if (apiPhotos.isNotEmpty()) {
                    allPhotos.clear()
                    allPhotos.addAll(
                        apiPhotos.map { dto ->
                            Photo(
                                id = dto.id.toInt(),
                                title = dto.title,
                                description = dto.description,
                                uri = Uri.parse(dto.imageUrl),
                                likes = 0,
                                isLiked = false,
                                authorName = currentUser?.fullName ?: dto.author,
                                authorAvatarUri = if (currentUser?.avatarUrl.isNullOrEmpty()) null else Uri.parse(currentUser!!.avatarUrl)
                            )
                        }
                    )
                } else if (allPhotos.isEmpty()) {
                    allPhotos.clear()
                    allPhotos.addAll(testPhotos)
                }
            }

            HomeScreen(
                photos = allPhotos.toList(),
                onPhotoClick = { photo -> navController.navigate("photo_detail/${photo.id}") },
                onLikeClick = onLikeClick,
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToDiscover = { navController.navigate("discover") },
                onNavigateToAddPhoto = { navController.navigate("add") }
            )
        }

        // 👤 PROFILE
        composable("profile") {
            val photoViewModel: PhotoViewModel = viewModel()
            val apiPhotos by photoViewModel.photos.collectAsState()
            val localContext = LocalContext.current

            LaunchedEffect(Unit) {
                photoViewModel.loadPhotos(localContext)
            }

            val userPhotos = apiPhotos.filter {
                it.author == (currentUser?.email ?: "")
            }.map { dto ->
                Photo(
                    id = dto.id.toInt(),
                    title = dto.title,
                    description = dto.description,
                    uri = Uri.parse(dto.imageUrl),
                    likes = 0,
                    isLiked = false,
                    authorName = currentUser?.fullName ?: "User",
                    authorAvatarUri = if (currentUser?.avatarUrl.isNullOrEmpty()) null else Uri.parse(currentUser!!.avatarUrl)
                )
            }

            ProfileScreen(
                userName = currentUser?.fullName ?: userName,
                avatarUri = if (currentUser?.avatarUrl.isNullOrEmpty()) userAvatarUri else Uri.parse(currentUser!!.avatarUrl),
                description = currentUser?.description ?: userDescription,
                myPhotos = if (userPhotos.isNotEmpty()) userPhotos else allPhotos.filter { it.authorName == (currentUser?.fullName ?: userName) },
                onSettings = { navController.navigate("settings") },
                onPhotoClick = { photo -> navController.navigate("photo_detail/${photo.id}") }
            )
        }

        // ⚙️ SETTINGS
        composable("settings") {
            // Локальные переменные для редактирования
            var editedName by remember { mutableStateOf(currentUser?.fullName ?: userName) }
            var editedDescription by remember { mutableStateOf(currentUser?.description ?: userDescription) }
            var editedAvatar by remember { mutableStateOf(authViewModel.getAvatarUri() ?: userAvatarUri) }

            SettingsScreen(
                currentName = editedName,
                currentAvatarUri = editedAvatar,
                currentDescription = editedDescription,

                // Просто обновляем локальное состояние при вводе
                onNameChange = { editedName = it },
                onDescriptionChange = { editedDescription = it },
                onAvatarChange = { newUri ->
                    editedAvatar = newUri  // Обновляем локально
                    // ✅ Загружаем аватар на сервер (НОВЫЙ метод!)
                    if (newUri != null) {
                        authViewModel.uploadAvatar(context, newUri)
                    }
                },

                onSave = {
                    println("🔵 [SAVE] Saving profile: name=$editedName, desc=$editedDescription, avatar=$editedAvatar")

                    // Обновляем локальный кэш
                    userName = editedName
                    userDescription = editedDescription
                    userAvatarUri = editedAvatar

                    // Отправляем на сервер ВСЕ данные
                    authViewModel.updateProfile(
                        context,
                        editedName,
                        editedDescription
                    )

                    navController.popBackStack()
                },

                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout(context)
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        // 🔍 DISCOVER
        composable("discover") {
            DiscoverProfilesScreen(
                profiles = listOf(
                    com.example.portfolioapp.entity.UserProfile(1, "Anna Visuals", "Street photography", Uri.parse("https://picsum.photos/seed/anna/100/100")),
                    com.example.portfolioapp.entity.UserProfile(2, "Max Travel", "Landscape creator", Uri.parse("https://picsum.photos/seed/max/100/100"))
                ),
                onBack = { navController.popBackStack() }
            )
        }

        // ➕ ADD PHOTO
        composable("add") {
            val photoViewModel: PhotoViewModel = viewModel()
            val localContext = LocalContext.current

            AddPhotoScreen(
                onSave = { title, desc, uri ->
                    photoViewModel.createPhoto(localContext, title, desc, uri)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 🖼️ PHOTO DETAIL
        composable(
            route = "photo_detail/{photoId}",
            arguments = listOf(navArgument("photoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getInt("photoId")
            val photo = allPhotos.find { it.id == photoId }
            if (photo != null) {
                PhotoDetailScreen(photo = photo, onBack = { navController.popBackStack() })
            } else {
                navController.popBackStack()
            }
        }
    }
}

// ============================================
// 📝 Экран регистрации
// ============================================
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()

    LaunchedEffect(authSuccess) {
        if (authSuccess != null) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(email, password, fullName) {
        viewModel.clearError()
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create Account", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            error?.let { msg ->
                Text(text = msg, color = Color(0xFFFF5252), fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.register(email, password, fullName, context) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Register", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBackToLogin) {
                Text("Already have an account? Login", color = Color(0xFF7C4DFF))
            }
        }
    }
}

// ============================================
// 🎨 Фоновый градиент
// ============================================
@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF09090B),
                        Color(0xFF18181B),
                        Color(0xFF27272A)
                    )
                )
            )
    ) {
        content()
    }
}