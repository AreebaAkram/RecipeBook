package com.recipebook.community.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.recipebook.community.data.model.Recipe
import com.recipebook.community.data.model.User
import com.recipebook.community.ui.components.DoodleBorder
import com.recipebook.community.ui.theme.BoldTeal
import com.recipebook.community.ui.theme.Coral
import com.recipebook.community.viewmodel.RecipeViewModel

@Composable
fun ApprovalsScreen(recipeVM: RecipeViewModel, user: User?) {
    val pending by recipeVM.pendingRecipes.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Approvals", style = MaterialTheme.typography.headlineLarge)
        Text("${pending.size} recipe${if (pending.size != 1) "s" else ""} waiting for your review",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Spacer(Modifier.height(16.dp))
        if (pending.isEmpty()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("All clear! No pending recipes", style = MaterialTheme.typography.headlineMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(pending, key = { it.id }) { recipe ->
                    ApprovalCard(recipe, user, recipeVM)
                }
            }
        }
    }
}

@Composable
fun ApprovalCard(recipe: Recipe, user: User?, recipeVM: RecipeViewModel) {
    var approved by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (approved) 1.05f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "approve_scale")

    DoodleBorder(modifier = Modifier.fillMaxWidth(), borderColor = MaterialTheme.colorScheme.secondary) {
        Column(Modifier.padding(16.dp)) {
            Text(recipe.title, style = MaterialTheme.typography.titleLarge)
            Text("by ${recipe.authorName} - ${recipe.category}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text(recipe.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        approved = true
                        if (user != null) recipeVM.approveRecipe(recipe, user.uid, user.displayName)
                    },
                    modifier = Modifier.weight(1f).scale(scale),
                    colors   = ButtonDefaults.buttonColors(containerColor = BoldTeal)
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Approve")
                }
                OutlinedButton(
                    onClick = { if (user != null) recipeVM.rejectRecipe(recipe, user.uid, user.displayName) },
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Coral)
                ) {
                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reject")
                }
            }
        }
    }
}
