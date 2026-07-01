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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Text("Approvals 🔍", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "${pending.size} recipe${if (pending.size != 1) "s" else ""} waiting for review",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))

        if (pending.isEmpty()) {
            Column(
                modifier            = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("✅", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("All clear! No pending recipes",
                    style     = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center)
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
    // FIX #2: track action state locally per card so button gives visual feedback
    var actionTaken by remember { mutableStateOf<String?>(null) }
    val scale by animateFloatAsState(
        targetValue   = if (actionTaken == "approved") 1.03f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label         = "approve_scale"
    )

    DoodleBorder(
        modifier    = Modifier.fillMaxWidth().scale(scale),
        borderColor = when (actionTaken) {
            "approved" -> BoldTeal
            "rejected" -> Coral
            else       -> MaterialTheme.colorScheme.secondary
        }
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header — title + edit badge if this is a re-submission
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(recipe.title,
                    style    = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f))
                // FIX #1: show "Re-edit" badge so admin knows this
                // is an edited recipe, not a fresh submission
                if (recipe.updatedAt > recipe.createdAt) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text("✏️ Edited",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("by ${recipe.authorName} · ${recipe.category}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            if (recipe.description.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(recipe.description,
                    style    = MaterialTheme.typography.bodyMedium,
                    maxLines = 3)
            }
            Spacer(Modifier.height(4.dp))
            Text("${recipe.ingredients.size} ingredients · ${recipe.steps.size} steps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            Spacer(Modifier.height(12.dp))

            if (actionTaken != null) {
                // FIX #2: show confirmation so admin can see the action fired
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = if (actionTaken == "approved")
                        BoldTeal.copy(alpha = 0.12f)
                    else
                        Coral.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (actionTaken == "approved") Icons.Default.CheckCircle
                            else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (actionTaken == "approved") BoldTeal else Coral
                        )
                        Text(
                            if (actionTaken == "approved")
                                "Approved! Recipe is now live."
                            else
                                "Rejected. Recipe won't be published.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (actionTaken == "approved") BoldTeal else Coral
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // FIX #2: guard against null user and empty communityId
                    Button(
                        onClick = {
                            if (user != null) {
                                actionTaken = "approved"
                                recipeVM.approveRecipe(recipe, user.uid, user.displayName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled  = user != null,
                        colors   = ButtonDefaults.buttonColors(containerColor = BoldTeal)
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = {
                            if (user != null) {
                                actionTaken = "rejected"
                                recipeVM.rejectRecipe(recipe, user.uid, user.displayName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled  = user != null,
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Coral)
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reject")
                    }
                }
            }
        }
    }
}

