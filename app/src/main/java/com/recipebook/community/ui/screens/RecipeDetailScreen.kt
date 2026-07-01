
package com.recipebook.community.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.recipebook.community.data.model.User
import com.recipebook.community.ui.components.AvatarView
import com.recipebook.community.ui.components.CategoryChip
import com.recipebook.community.ui.components.DoodleDivider
import com.recipebook.community.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId : String,
    recipeVM : RecipeViewModel,
    user     : User?,
    onBack   : () -> Unit,
    onEdit   : (String) -> Unit
) {
    val approved by recipeVM.approvedRecipes.collectAsState()
    val pending  by recipeVM.pendingRecipes.collectAsState()
    val recipe   = (approved + pending).find { it.id == recipeId } ?: run { onBack(); return }

    val isAdmin  = user?.role == "admin"
    val isAuthor = user?.uid  == recipe.authorUid

    // FIX 1: separate permission rules
    // - canEdit: any community member can propose an edit to ANY recipe
    // - canDelete: only the recipe's author OR an admin can delete it
    val canEdit   = user != null               // every signed-in member can edit any recipe
    val canDelete = isAdmin || isAuthor

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val imageBitmap: ImageBitmap? = remember(recipe.imageUrl) {
        if (recipe.imageUrl.isEmpty()) null
        else try {
            val bytes = android.util.Base64.decode(recipe.imageUrl, android.util.Base64.NO_WRAP)
            android.graphics.BitmapFactory
                .decodeByteArray(bytes, 0, bytes.size)
                ?.asImageBitmap()
        } catch (e: Exception) { null }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Recipe?") },
            text  = { Text("\"${recipe.title}\" will be permanently removed. This can't be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        recipeVM.deleteRecipe(recipe, user!!.uid, user.displayName)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(spring(Spring.DampingRatioMediumBouncy)) { it / 2 } +
                fadeIn(tween(300))
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title          = {
                        Text(recipe.title, maxLines = 1,
                            style = MaterialTheme.typography.titleLarge)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions        = {
                        if (canEdit) {
                            IconButton(onClick = { onEdit(recipe.id) }) {
                                Icon(Icons.Default.Edit, "Edit",
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (canDelete) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, "Delete",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap             = imageBitmap,
                        contentDescription = recipe.title,
                        modifier           = Modifier.fillMaxWidth().height(240.dp),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(160.dp)
                            .background(
                                com.recipebook.community.ui.theme
                                    .categoryColor(recipe.category).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val emoji = when (recipe.category) {
                            "Breakfast" -> "🍳"; "Lunch" -> "🥗"; "Dinner"   -> "🍽️"
                            "Snacks"    -> "🍿"; "Desserts" -> "🍰"; "Drinks" -> "🥤"
                            else        -> "🍜"
                        }
                        Text(emoji, style = MaterialTheme.typography.displayLarge)
                    }
                }

                Column(Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryChip(recipe.category)
                        if (recipe.featured) {
                            Surface(shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondary) {
                                Text("⭐ Featured",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                        AvatarView(name = recipe.authorName, colorHex = "#1D9E75", size = 24.dp)
                        Text("by ${recipe.authorName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.height(10.dp))

                    Text(recipe.title, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(recipe.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoPill("⏱ ${recipe.cookTime}")
                        InfoPill("🍽 ${recipe.servings} servings")
                    }
                    Spacer(Modifier.height(20.dp))
                    DoodleDivider(Modifier.fillMaxWidth().height(14.dp))
                    Spacer(Modifier.height(16.dp))

                    Text("Ingredients", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    recipe.ingredients.forEach { ing ->
                        Row(modifier = Modifier.padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("•", color = MaterialTheme.colorScheme.primary)
                            Text(ing, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    DoodleDivider(Modifier.fillMaxWidth().height(14.dp))
                    Spacer(Modifier.height(16.dp))

                    Text("Steps", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    recipe.steps.forEachIndexed { i, step ->
                        Row(modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelLarge)
                            }
                            Text(step, style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun InfoPill(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge)
    }
}

