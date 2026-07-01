
package com.recipebook.community.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recipebook.community.data.model.Recipe
import com.recipebook.community.data.model.User
import com.recipebook.community.ui.components.*
import com.recipebook.community.ui.theme.categoryColor
import com.recipebook.community.viewmodel.RecipeViewModel
import kotlin.math.sin

val CATEGORIES = listOf("All", "Breakfast", "Lunch", "Dinner", "Snacks", "Desserts", "Drinks")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    recipeVM      : RecipeViewModel,
    user          : User?,
    onRecipeClick : (String) -> Unit,
    onAddClick    : () -> Unit,
    onProfileClick: () -> Unit
) {
    val recipes  by recipeVM.approvedRecipes.collectAsState()
    val loading  by recipeVM.isLoading.collectAsState()
    var search   by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    val filtered = recipes.filter { r ->
        (category == "All" || r.category == category) &&
                (search.isBlank() || r.title.contains(search, ignoreCase = true) ||
                        r.description.contains(search, ignoreCase = true))
    }

    Scaffold(
        floatingActionButton = { WigglingFAB(onClick = onAddClick) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier       = Modifier.padding(padding)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().drawBehind {
                        drawCircle(Color(0x221D9E75), 80f, Offset(size.width - 60f, 60f))
                        drawCircle(Color(0x22EF9F27), 50f, Offset(40f, 80f))
                        drawCircle(Color(0x22FF6B6B), 35f, Offset(size.width - 30f, 130f))
                        val path = androidx.compose.ui.graphics.Path()
                        for (i in 0..100) {
                            val t = i / 100f
                            val x = t * size.width
                            val y = 10f + sin(t * 6 * Math.PI.toFloat()) * 6f
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, Color(0x441D9E75), style = Stroke(width = 3f))
                    }
                ) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Spacer(Modifier.height(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Hey ${user?.displayName?.split(" ")?.first() ?: "Chef"} 👋",
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                Text(
                                    "What's cooking today?",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                            AvatarView(
                                name        = user?.displayName ?: "?",
                                colorHex    = user?.avatarColor  ?: "#1D9E75",
                                size        = 48.dp,
                                avatarSeed  = user?.avatarSeed   ?: "",
                                avatarStyle = user?.avatarStyle  ?: "adventurer",
                                modifier    = Modifier.clickable { onProfileClick() }
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value         = search,
                            onValueChange = { search = it },
                            placeholder   = { Text("Search recipes…") },
                            leadingIcon   = { Icon(Icons.Default.Search, null) },
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(50),
                            singleLine    = true
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            item {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CATEGORIES) { cat ->
                        CategoryChip(
                            category = cat,
                            selected = category == cat,
                            onClick  = { category = cat }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                DoodleDivider(Modifier.fillMaxWidth().height(16.dp).padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))
            }

            // ── BANNER AD — sits between filters and recipe list ──
            item {
                BannerAdView(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                Spacer(Modifier.height(8.dp))
            }

            when {
                loading            -> item { RecipeCardSkeleton() }
                filtered.isEmpty() -> item { EmptyState() }
                else               -> itemsIndexed(filtered) { index, recipe ->
                    AnimatedRecipeCard(recipe, index, onRecipeClick)
                }
            }
        }
    }
}

@Composable
fun AnimatedRecipeCard(recipe: Recipe, index: Int, onClick: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val imageBitmap: ImageBitmap? = remember(recipe.imageUrl) {
        if (recipe.imageUrl.isEmpty()) null
        else try {
            val bytes = android.util.Base64.decode(recipe.imageUrl, android.util.Base64.NO_WRAP)
            android.graphics.BitmapFactory
                .decodeByteArray(bytes, 0, bytes.size)
                ?.asImageBitmap()
        } catch (e: Exception) { null }
    }

    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(
            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
        ) { it } + fadeIn()
    ) {
        DoodleBorder(
            modifier       = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clickable { onClick(recipe.id) },
            borderColor    = categoryColor(recipe.category),
            showCornerDots = true
        ) {
            Row(
                modifier              = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap             = imageBitmap,
                        contentDescription = recipe.title,
                        modifier           = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    val emoji = when (recipe.category) {
                        "Breakfast" -> "🍳"; "Lunch" -> "🥗"; "Dinner"   -> "🍽️"
                        "Snacks"    -> "🍿"; "Desserts" -> "🍰"; "Drinks" -> "🥤"
                        else        -> "🍜"
                    }
                    Box(
                        modifier         = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(categoryColor(recipe.category).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Text(emoji, fontSize = 36.sp) }
                }

                Column(Modifier.weight(1f)) {
                    CategoryChip(recipe.category)
                    Spacer(Modifier.height(4.dp))
                    Text(recipe.title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
                    Text("by ${recipe.authorName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, null, Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Text(recipe.cookTime, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.People, null, Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary)
                        Text(recipe.servings, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🍳", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("No recipes yet — feed the family!",
            style     = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Add the first recipe and get the party started 🎉",
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    }
}