package com.recipebook.community.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.recipebook.community.ui.components.DoodleBorder
import com.recipebook.community.viewmodel.AuthState
import com.recipebook.community.viewmodel.AuthViewModel

val AVATAR_COLORS = listOf("#1D9E75","#EF9F27","#FF6B6B","#9B72CF","#5BB8E8","#E07BBD")

@Composable
fun SignUpScreen(authVM: AuthViewModel, onBack: () -> Unit) {
    var name         by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(AVATAR_COLORS[0]) }
    val state        by authVM.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))

        DoodleBorder(modifier = Modifier.fillMaxWidth(), borderColor = MaterialTheme.colorScheme.secondary) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(password, { password = it }, label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))

                Text("Pick your avatar color:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AVATAR_COLORS.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(color)
                                .border(if (selectedColor == hex) 3.dp else 0.dp, Color.Black, CircleShape)
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        if (state is AuthState.Error)
            Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error)

        Spacer(Modifier.height(8.dp))
        Button(onClick = { authVM.signUp(name, email, password, selectedColor) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = state !is AuthState.Loading) {
            if (state is AuthState.Loading) CircularProgressIndicator(Modifier.size(20.dp))
            else Text("Create Account")
        }
        TextButton(onClick = onBack) { Text("Already have an account? Sign in") }
    }
}
