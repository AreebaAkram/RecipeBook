package com.recipebook.community.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.recipebook.community.ui.components.DoodleBorder
import com.recipebook.community.viewmodel.AuthState
import com.recipebook.community.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authVM: AuthViewModel, onSignUp: () -> Unit) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state    by authVM.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Text("Recipe Book", style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Your community kitchen!", style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Spacer(Modifier.height(40.dp))

        DoodleBorder(modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.primary) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(value = password, onValueChange = { password = it },
                    label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            }
        }

        Spacer(Modifier.height(20.dp))

        if (state is AuthState.Error) {
            Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(onClick = { authVM.signIn(email, password) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = state !is AuthState.Loading) {
            if (state is AuthState.Loading) CircularProgressIndicator(Modifier.size(20.dp))
            else Text("Sign In")
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onSignUp) { Text("Don't have an account? Sign up") }
    }
}
