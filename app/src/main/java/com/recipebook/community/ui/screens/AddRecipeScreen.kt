
package com.recipebook.community.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.recipebook.community.data.model.User
import com.recipebook.community.ui.components.DoodleBorder
import com.recipebook.community.ui.components.DoodleDivider
import com.recipebook.community.viewmodel.AdManager
import com.recipebook.community.viewmodel.RecipeViewModel
import com.recipebook.community.viewmodel.RewardedAdState
import java.io.ByteArrayOutputStream

// ── Draft helpers (unchanged from before) ──────────────────
private const val PREF_DRAFT = "recipe_draft"
private fun saveDraft(ctx: Context, title: String, desc: String, category: String,
                      cookTime: String, servings: String, ingredients: List<String>,
                      steps: List<String>, imageB64: String) {
    ctx.getSharedPreferences(PREF_DRAFT, Context.MODE_PRIVATE).edit()
        .putString("title", title).putString("desc", desc)
        .putString("category", category).putString("cookTime", cookTime)
        .putString("servings", servings)
        .putString("ingredients", ingredients.joinToString("||"))
        .putString("steps", steps.joinToString("||"))
        .putString("imageB64", imageB64).apply()
}
private fun clearDraft(ctx: Context) {
    ctx.getSharedPreferences(PREF_DRAFT, Context.MODE_PRIVATE).edit().clear().apply()
}
private fun loadDraft(ctx: Context): Map<String, String> {
    val p = ctx.getSharedPreferences(PREF_DRAFT, Context.MODE_PRIVATE)
    return mapOf(
        "title"       to (p.getString("title", "") ?: ""),
        "desc"        to (p.getString("desc", "") ?: ""),
        "category"    to (p.getString("category", "Breakfast") ?: "Breakfast"),
        "cookTime"    to (p.getString("cookTime", "") ?: ""),
        "servings"    to (p.getString("servings", "") ?: ""),
        "ingredients" to (p.getString("ingredients", "") ?: ""),
        "steps"       to (p.getString("steps", "") ?: ""),
        "imageB64"    to (p.getString("imageB64", "") ?: "")
    )
}

// ── Convert URI → base64 ONCE (off the main thread) ────────
fun uriToBase64(ctx: Context, uri: Uri): String {
    return try {
        val stream = ctx.contentResolver.openInputStream(uri) ?: return ""
        val bitmap = BitmapFactory.decodeStream(stream)
        val scale  = 600f / maxOf(bitmap.width, 1)
        val scaled = if (scale < 1f)
            Bitmap.createScaledBitmap(bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(), true)
        else bitmap
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 75, out)
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP) // NO_WRAP avoids newline artifacts
    } catch (e: Exception) { "" }
}

// ── Decode base64 → ImageBitmap ONCE and remember it ───────
@Composable
private fun rememberBitmapFromB64(b64: String): ImageBitmap? {
    return remember(b64) {                       // only re-runs when b64 actually changes
        if (b64.isEmpty()) null
        else try {
            val bytes = Base64.decode(b64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) { null }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(recipeVM: RecipeViewModel, user: User?, onDone: () -> Unit) {
    val ctx     = LocalContext.current
    val draft   = remember { loadDraft(ctx) }
    val isAdmin = user?.role == "admin"

    var title       by remember { mutableStateOf(draft["title"]!!) }
    var description by remember { mutableStateOf(draft["desc"]!!) }
    var category    by remember { mutableStateOf(draft["category"]!!) }
    var cookTime    by remember { mutableStateOf(draft["cookTime"]!!) }
    var servings    by remember { mutableStateOf(draft["servings"]!!) }
    var ingredients by remember { mutableStateOf(
        draft["ingredients"]!!.split("||").filter { it.isNotEmpty() }.ifEmpty { listOf("") }) }
    var steps       by remember { mutableStateOf(
        draft["steps"]!!.split("||").filter { it.isNotEmpty() }.ifEmpty { listOf("") }) }

    // FIX 1: imageB64 is a separate state — changes to text fields
    // do NOT touch imageB64, so the image never blinks
    var imageB64    by remember { mutableStateOf(draft["imageB64"]!!) }

    // FIX 2: decode once, re-decode only when imageB64 itself changes
    val imageBitmap = rememberBitmapFromB64(imageB64)

    var catExpanded by remember { mutableStateOf(false) }
    val loading     by recipeVM.isLoading.collectAsState()
    val adManager: AdManager = viewModel()
    val adState by adManager.state.collectAsState()
    val activity = LocalContext.current as? android.app.Activity
    var isFeatured by remember { mutableStateOf(false) }

    // Save draft — split into two LaunchedEffects so image changes
    // don't force text-field recomposition and vice-versa
    LaunchedEffect(title, description, category, cookTime, servings, ingredients, steps) {
        saveDraft(ctx, title, description, category, cookTime, servings, ingredients, steps, imageB64)
    }
    LaunchedEffect(imageB64) {
        saveDraft(ctx, title, description, category, cookTime, servings, ingredients, steps, imageB64)
    }
    LaunchedEffect(Unit) {
        adManager.loadRewardedAd(ctx)
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Run conversion on IO thread so UI stays smooth
            imageB64 = uriToBase64(ctx, uri)
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Add Recipe 📝", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        if (isAdmin)
            Text("As admin, your recipe is published instantly ✨",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))

        // ── Image picker — uses decoded bitmap directly (no AsyncImage round-trip) ──
        Box(
            modifier = Modifier
                .fillMaxWidth().height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap           = imageBitmap,
                    contentDescription = "Recipe photo",
                    modifier         = Modifier.fillMaxSize(),
                    contentScale     = ContentScale.Crop
                )
                IconButton(
                    onClick  = { imageB64 = "" },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, null, Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text("Tap to add photo (optional)",
                        style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(title, { title = it },
            label    = { Text("Recipe Title *") },
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(description, { description = it },
            label    = { Text("Description") },
            modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
            OutlinedTextField(category, {}, label = { Text("Category") },
                modifier = Modifier.fillMaxWidth().menuAnchor(), readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) })
            ExposedDropdownMenu(catExpanded, { catExpanded = false }) {
                CATEGORIES.drop(1).forEach { cat ->
                    DropdownMenuItem(
                        text    = { Text(cat) },
                        onClick = { category = cat; catExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(cookTime, { cookTime = it },
                label       = { Text("Cook Time") },
                modifier    = Modifier.weight(1f),
                placeholder = { Text("e.g. 30 min") })
            OutlinedTextField(servings, { servings = it },
                label       = { Text("Servings") },
                modifier    = Modifier.weight(1f),
                placeholder = { Text("e.g. 4") })
        }
        Spacer(Modifier.height(16.dp))

        // ── Ingredients ──────────────────────────────────────────
        DoodleBorder(modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.secondary) {
            Column(Modifier.padding(16.dp)) {
                Text("🧂 Ingredients", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                ingredients.forEachIndexed { i, ing ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${i + 1}.", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value         = ing,
                            onValueChange = { v ->
                                ingredients = ingredients.toMutableList().also { it[i] = v }
                            },
                            label    = { Text("Ingredient ${i + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        if (ingredients.size > 1) {
                            IconButton(onClick = {
                                ingredients = ingredients.toMutableList().also { it.removeAt(i) }
                            }) {
                                Icon(Icons.Default.Remove, null,
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                TextButton(onClick = { ingredients = ingredients + "" }) {
                    Icon(Icons.Default.Add, null); Text(" Add Ingredient")
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // ── Steps ────────────────────────────────────────────────
        DoodleBorder(modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.tertiary) {
            Column(Modifier.padding(16.dp)) {
                Text("👩‍🍳 Steps", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                steps.forEachIndexed { i, step ->
                    Row(verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${i + 1}.",
                            style    = MaterialTheme.typography.labelLarge,
                            color    = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 16.dp))
                        OutlinedTextField(
                            value         = step,
                            onValueChange = { v ->
                                steps = steps.toMutableList().also { it[i] = v }
                            },
                            label    = { Text("Step ${i + 1}") },
                            modifier = Modifier.weight(1f),
                            minLines = 2
                        )
                        if (steps.size > 1) {
                            IconButton(
                                onClick  = {
                                    steps = steps.toMutableList().also { it.removeAt(i) }
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.Remove, null,
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                TextButton(onClick = { steps = steps + "" }) {
                    Icon(Icons.Default.Add, null); Text(" Add Step")
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        DoodleBorder(
            modifier    = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.tertiary
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text("Feature this recipe ✨", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Watch a short ad to get your recipe highlighted at the top of Home",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(10.dp))

                if (isFeatured) {
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                        Text("✅ Will be featured!",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Button(
                        onClick = {
                            activity?.let {
                                adManager.showRewardedAd(
                                    activity = it,
                                    onReward = { isFeatured = true }
                                )
                            }
                        },
                        enabled = adState is RewardedAdState.Ready,
                        colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        when (adState) {
                            is RewardedAdState.Loading -> {
                                CircularProgressIndicator(Modifier.size(16.dp), color = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Loading ad…")
                            }
                            is RewardedAdState.Ready -> Text("▶ Watch Ad to Feature")
                            else -> Text("Ad unavailable")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

      Button(
          onClick = {
              if (user != null && title.isNotBlank()) {
                  recipeVM.submitRecipe(
                      title, description, category, cookTime, servings,
                      ingredients, steps, imageB64,
                      user.uid, user.displayName, isAdmin,
                      featured = isFeatured        // ← THIS WAS MISSING
                  ) { success ->
                      if (success) { clearDraft(ctx); onDone() }
                  }
              }
          },
          modifier = Modifier.fillMaxWidth().height(52.dp),
          enabled  = !loading && title.isNotBlank()
      ) {
          if (loading) CircularProgressIndicator(Modifier.size(20.dp))
          else Text(if (isAdmin) "Submit Recipe 🍽️" else "Submit for Approval 🚀")
      }
        Spacer(Modifier.height(24.dp))
    }
}