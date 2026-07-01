package com.recipebook.community.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipebook.community.viewmodel.AuthState
import com.recipebook.community.viewmodel.AuthViewModel

@Composable
fun CommunitySetupScreen(authVM: AuthViewModel) {
    var mode          by remember { mutableStateOf<String?>(null) }
    var communityName by remember { mutableStateOf("") }
    var inviteCode    by remember { mutableStateOf("") }
    val state         by authVM.authState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Join a Kitchen", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("Create your own community or join one with a code",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Spacer(Modifier.height(40.dp))

        if (mode == null) {
            Button(onClick = { mode = "create" }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Create a Community")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { mode = "join" }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Join with Invite Code")
            }
        } else if (mode == "create") {
            OutlinedTextField(communityName, { communityName = it },
                label = { Text("Community Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            if (state is AuthState.Error)
                Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
            Button(onClick = { authVM.createCommunity(communityName) },
                modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Create!") }
            TextButton(onClick = { mode = null }) { Text("<- Back") }
        } else {
            OutlinedTextField(inviteCode, { inviteCode = it.uppercase() },
                label = { Text("Invite Code (e.g. ABC123)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            if (state is AuthState.Error)
                Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
            Button(onClick = { authVM.joinCommunity(inviteCode) },
                modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Join!") }
            TextButton(onClick = { mode = null }) { Text("<- Back") }
        }
    }
}
