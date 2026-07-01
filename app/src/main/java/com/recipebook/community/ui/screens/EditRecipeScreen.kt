package com.recipebook.community.ui.screens


import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.recipebook.community.data.model.User
import com.recipebook.community.ui.components.DoodleBorder
import com.recipebook.community.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: String,
    recipeVM: RecipeViewModel,
    user    : User?,
    onDone  : () -> Unit
) {
    val ctx     = LocalContext.current
    val isAdmin = user?.role == "admin"
    val recipes by recipeVM.approvedRecipes.collectAsState()
    val pending by recipeVM.pendingRecipes.collectAsState()
    val recipe  = (recipes + pending).find { it.id == recipeId } ?: run { onDone(); return }

    var title       by remember { mutableStateOf(recipe.title) }
    var description by remember { mutableStateOf(recipe.description) }
    var category    by remember { mutableStateOf(recipe.category) }
    var cookTime    by remember { mutableStateOf(recipe.cookTime) }
    var servings    by remember { mutableStateOf(recipe.servings) }
    var ingredients by remember { mutableStateOf(recipe.ingredients.ifEmpty { listOf("") }) }
    var steps       by remember { mutableStateOf(recipe.steps.ifEmpty { listOf("") }) }
    var imageB64    by remember { mutableStateOf(recipe.imageUrl) }
    var catExpanded by remember { mutableStateOf(false) }
    val loading     by recipeVM.isLoading.collectAsState()

    val imageBitmap = remember(imageB64) {
        if (imageB64.isEmpty()) null
        else try {
            val bytes = Base64.decode(imageB64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) { null }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageB64 = uriToBase64(ctx, uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Edit Recipe ✏️") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(pad).padding(20.dp)
        ) {
            if (!isAdmin)
                Text("Editing will re-submit for admin approval",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))

            // Image picker
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap             = imageBitmap,
                        contentDescription = "Recipe photo",
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                    IconButton(
                        onClick  = { imageB64 = "" },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null,
                            tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Text("Tap to change photo",
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

            ExposedDropdownMenuBox(expanded = catExpanded,
                onExpandedChange = { catExpanded = it }) {
                OutlinedTextField(category, {}, label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) })
                ExposedDropdownMenu(catExpanded, { catExpanded = false }) {
                    CATEGORIES.drop(1).forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) },
                            onClick = { category = cat; catExpanded = false })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(cookTime, { cookTime = it },
                    label = { Text("Cook Time") }, modifier = Modifier.weight(1f))
                OutlinedTextField(servings, { servings = it },
                    label = { Text("Servings") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))

            // Ingredients
            DoodleBorder(modifier = Modifier.fillMaxWidth(),
                borderColor = MaterialTheme.colorScheme.secondary) {
                Column(Modifier.padding(16.dp)) {
                    Text("🧂 Ingredients", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    ingredients.forEachIndexed { i, ing ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${i+1}.", color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge)
                            OutlinedTextField(ing,
                                { v -> ingredients = ingredients.toMutableList().also { it[i] = v } },
                                label = { Text("Ingredient ${i+1}") }, modifier = Modifier.weight(1f))
                            if (ingredients.size > 1)
                                IconButton(onClick = {
                                    ingredients = ingredients.toMutableList().also { it.removeAt(i) }
                                }) { Icon(Icons.Default.Remove, null,
                                    tint = MaterialTheme.colorScheme.error) }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    TextButton(onClick = { ingredients = ingredients + "" }) {
                        Icon(Icons.Default.Add, null); Text(" Add Ingredient") }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Steps
            DoodleBorder(modifier = Modifier.fillMaxWidth(),
                borderColor = MaterialTheme.colorScheme.tertiary) {
                Column(Modifier.padding(16.dp)) {
                    Text("👩‍🍳 Steps", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    steps.forEachIndexed { i, step ->
                        Row(verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${i+1}.", color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(top = 16.dp))
                            OutlinedTextField(step,
                                { v -> steps = steps.toMutableList().also { it[i] = v } },
                                label = { Text("Step ${i+1}") },
                                modifier = Modifier.weight(1f), minLines = 2)
                            if (steps.size > 1)
                                IconButton(onClick = {
                                    steps = steps.toMutableList().also { it.removeAt(i) }
                                }, modifier = Modifier.padding(top = 8.dp)) {
                                    Icon(Icons.Default.Remove, null,
                                        tint = MaterialTheme.colorScheme.error) }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    TextButton(onClick = { steps = steps + "" }) {
                        Icon(Icons.Default.Add, null); Text(" Add Step") }
                }
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (user != null && title.isNotBlank()) {
                        val updated = recipe.copy(
                            title       = title,
                            description = description,
                            category    = category,
                            cookTime    = cookTime,
                            servings    = servings,
                            ingredients = ingredients.filter { it.isNotBlank() },
                            steps       = steps.filter { it.isNotBlank() },
                            imageUrl    = imageB64
                        )
                        recipeVM.editRecipe(updated, user.uid, user.displayName, isAdmin)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !loading && title.isNotBlank()
            ) {
                Text(if (isAdmin) "Save Changes ✅" else "Save & Re-submit 🚀")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}