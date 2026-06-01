package com.example.portfolioapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.portfolioapp.presentation.components.GradientBackground
import com.example.portfolioapp.viewModel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()

    // 🔄 Переход после успешного входа
    LaunchedEffect(authSuccess) {
        if (authSuccess != null) {
            onLoginSuccess()
            viewModel.clearAuthState() // Сбрасываем, чтобы не сработало повторно
        }
    }

    // 🧹 Сброс ошибки при изменении полей
    LaunchedEffect(email, password) {
        if (error != null) viewModel.clearError()
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PHOTOVERSE",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Your visual story starts here",
                color = Color.LightGray,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(40.dp))

            // ❌ Ошибка от сервера
            error?.let { msg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFF5252).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFFFF5252),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // 📧 Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF7C4DFF),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFF7C4DFF)
                )
            )
            Spacer(Modifier.height(16.dp))

            // 🔒 Password (с переключением видимости)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = Color.LightGray)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF7C4DFF),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFF7C4DFF)
                )
            )
            Spacer(Modifier.height(32.dp))

            // 🚀 Кнопка входа
            Button(
                onClick = { viewModel.login(email, password, context) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Explore Portfolio", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Create account", color = Color(0xFF7C4DFF))
            }
        }
    }
}