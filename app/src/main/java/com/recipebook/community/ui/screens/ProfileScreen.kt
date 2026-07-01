package com.recipebook.community.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recipebook.community.data.FirebaseService
import com.recipebook.community.data.model.CommunityMember
import com.recipebook.community.data.model.User
import com.recipebook.community.ui.components.AVATAR_OPTIONS
import com.recipebook.community.ui.components.AvatarOption
import com.recipebook.community.ui.components.AvatarView
import com.recipebook.community.ui.components.DoodleBorder
import com.recipebook.community.ui.components.DoodleDivider
import com.recipebook.community.ui.theme.Coral
import com.recipebook.community.viewmodel.AuthViewModel
import com.recipebook.community.viewmodel.CommunityViewModel
import com.recipebook.community.viewmodel.RecipeViewModel
import kotlin.math.sin

// ── Avatar Picker Dialog ─────────────────────────────────────
@Composable
fun AvatarPickerDialog(
    currentSeed : String,
    currentStyle: String,
    currentColor: String,
    displayName : String,
    onDismiss   : () -> Unit,
    onSave      : (seed: String, style: String) -> Unit
) {
    var selectedOption by remember {
        mutableStateOf(
            AVATAR_OPTIONS.find { it.seed == currentSeed } ?: AVATAR_OPTIONS[0]
        )
    }

    val inf = rememberInfiniteTransition(label = "preview_pulse")
    val previewScale by inf.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label         = "pulse"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choose Avatar 🧑‍🎨", style = MaterialTheme.typography.headlineMedium)
        },
        text = {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live preview
                Box(
                    modifier         = Modifier.size(90.dp).scale(previewScale),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarView(
                        name        = displayName,
                        colorHex    = currentColor,
                        size        = 80.dp,
                        avatarSeed  = selectedOption.seed,
                        avatarStyle = selectedOption.style
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    selectedOption.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text("Pick your look:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))

                // 2-column grid
                AVATAR_OPTIONS.chunked(2).forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { option ->
                            val isSelected = option.seed == selectedOption.seed
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(
                                        if (isSelected)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedOption = option }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarView(
                                    name        = displayName,
                                    colorHex    = currentColor,
                                    size        = 56.dp,
                                    avatarSeed  = option.seed,
                                    avatarStyle = option.style
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    option.label,
                                    fontSize = 11.sp,
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedOption.seed, selectedOption.style) }) {
                Text("Save Avatar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Stat Box ─────────────────────────────────────────────────
@Composable
fun StatBox(label: String, value: String, emoji: String) {
    DoodleBorder(
        borderColor    = MaterialTheme.colorScheme.secondary,
        showCornerDots = false,
        modifier       = Modifier.padding(4.dp)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(value,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary)
            Text(label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

// ── Main Profile Screen ──────────────────────────────────────
@Composable
fun ProfileScreen(
    authVM  : AuthViewModel,
    recipeVM: RecipeViewModel,
    commVM  : CommunityViewModel,
    user    : User?
) {
    val community by commVM.community.collectAsState()
    val members   by commVM.members.collectAsState()
    val logs      by recipeVM.activityLogs.collectAsState()
    val recipes   by recipeVM.approvedRecipes.collectAsState()
    val context   = LocalContext.current
    val isAdmin   = user?.role == "admin"
    val myLogs    = logs.filter { it.userUid == user?.uid }

    var showAvatarPicker by remember { mutableStateOf(false) }
    val myPending  = recipeVM.pendingRecipes.collectAsState().value
        .filter { it.authorUid == user?.uid }
    val myRejected = recipeVM.approvedRecipes.collectAsState().value

    if (showAvatarPicker) {
        AvatarPickerDialog(
            currentSeed  = user?.avatarSeed  ?: "",
            currentStyle = user?.avatarStyle ?: "adventurer",
            currentColor = user?.avatarColor ?: "#1D9E75",
            displayName  = user?.displayName ?: "",
            onDismiss    = { showAvatarPicker = false },
            onSave       = { seed, style ->
                val uid = user?.uid ?: return@AvatarPickerDialog
                FirebaseService.userRef(uid).child("avatarSeed").setValue(seed)
                FirebaseService.userRef(uid).child("avatarStyle").setValue(style)
                showAvatarPicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .drawBehind {
                drawCircle(Color(0x111D9E75), 200f, Offset(size.width, 0f))
                drawCircle(Color(0x11EF9F27), 140f, Offset(0f, size.height * 0.25f))
                drawCircle(Color(0x11FF6B6B), 100f, Offset(size.width * 0.85f, size.height * 0.5f))
                drawCircle(Color(0x119B72CF), 80f,  Offset(size.width * 0.1f, size.height * 0.75f))
                val path = androidx.compose.ui.graphics.Path()
                for (i in 0..100) {
                    val t = i / 100f
                    val x = t * size.width
                    val y = 8f + sin(t * 5 * Math.PI.toFloat()) * 5f
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Color(0x331D9E75), style = Stroke(width = 3f))
            }
            .padding(20.dp)
    ) {

        // ── Header: Avatar + Name ────────────────────────────
        DoodleBorder(
            modifier    = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.primary
        ) {
            Row(
                modifier              = Modifier.padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tappable avatar with edit badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    AvatarView(
                        name        = user?.displayName ?: "?",
                        colorHex    = user?.avatarColor  ?: "#1D9E75",
                        size        = 72.dp,
                        avatarSeed  = user?.avatarSeed   ?: "",
                        avatarStyle = user?.avatarStyle  ?: "adventurer",
                        modifier    = Modifier.clickable { showAvatarPicker = true }
                    )
                    Surface(
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showAvatarPicker = true }
                    ) {
                        Icon(
                            Icons.Default.Edit, "Edit avatar",
                            modifier = Modifier.padding(4.dp),
                            tint     = Color.White
                        )
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(user?.displayName ?: "",
                        style = MaterialTheme.typography.headlineMedium)
                    Text(user?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isAdmin) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.tertiary
                    ) {
                        Text(
                            text     = if (isAdmin) "👑 Admin" else "🧑‍🍳 Member",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelMedium,
                            color    = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        DoodleDivider(Modifier.fillMaxWidth().height(16.dp))
        Spacer(Modifier.height(16.dp))

        // ── Stats ────────────────────────────────────────────
        Text("📊 My Stats", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBox("Recipes", myLogs.count { it.action == "Added"   }.toString(), "✨")
            StatBox("Edits",   myLogs.count { it.action == "Edited"  }.toString(), "✏️")
            StatBox("Deleted", myLogs.count { it.action == "Deleted" }.toString(), "🗑️")
        }

        Spacer(Modifier.height(20.dp))
        DoodleDivider(Modifier.fillMaxWidth().height(16.dp))
        Spacer(Modifier.height(16.dp))

        // ── Community Members (visible to everyone) ──────────
        Text("👥 Community Members (${members.size})",
            style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))

        if (members.isEmpty()) {
            Text("No members yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        } else {
            members
                .sortedWith(
                    compareByDescending<CommunityMember> { it.role == "admin" }
                        .thenBy { it.displayName }
                )
                .forEach { member ->
                    val isSelf        = member.uid == user?.uid
                    val memberIsAdmin = member.role == "admin"

                    DoodleBorder(
                        modifier       = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        borderColor    = if (memberIsAdmin)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.tertiary,
                        showCornerDots = false
                    ) {
                        Row(
                            modifier              = Modifier.padding(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AvatarView(
                                name        = member.displayName,
                                colorHex    = member.avatarColor,
                                size        = 44.dp,
                                avatarSeed  = member.avatarSeed,
                                avatarStyle = member.avatarStyle
                            )
                            Column(Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(member.displayName,
                                        style = MaterialTheme.typography.bodyLarge)
                                    if (isSelf) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text("You",
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }
                                }
                                Text(member.email,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text  = if (memberIsAdmin) "👑 Admin" else "🧑‍🍳 Member",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (memberIsAdmin)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.tertiary
                                )
                            }
                            if (isAdmin && !isSelf && !memberIsAdmin) {
                                IconButton(onClick = {
                                    commVM.removeMember(member.uid)
                                }) {
                                    Icon(Icons.Default.PersonRemove, "Remove",
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
        }

        // ── Admin only: Invite Code + Export ─────────────────
        if (isAdmin && community != null) {
            Spacer(Modifier.height(20.dp))
            DoodleDivider(Modifier.fillMaxWidth().height(16.dp))
            Spacer(Modifier.height(16.dp))

            Text("🔑 Invite Code", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            DoodleBorder(
                modifier    = Modifier.fillMaxWidth(),
                borderColor = MaterialTheme.colorScheme.secondary
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Share this code to invite people",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text     = community!!.inviteCode,
                            style    = MaterialTheme.typography.displayLarge,
                            color    = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            val cm = context.getSystemService(
                                Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText(
                                "Invite Code", community!!.inviteCode))
                        }) {
                            Icon(Icons.Default.ContentCopy, "Copy",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    TextButton(onClick = {
                        commVM.regenerateInviteCode(user!!.communityId, community!!.inviteCode)
                    }) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Regenerate Code")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick  = {
                    val dl = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS)
                    commVM.exportData(user!!.communityId, recipes, dl) {}
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(Modifier.width(8.dp))
                Text("Export Community Data as JSON")
            }
        }

        // ── My Activity ──────────────────────────────────────
        Spacer(Modifier.height(20.dp))
        DoodleDivider(Modifier.fillMaxWidth().height(16.dp))
        Spacer(Modifier.height(16.dp))

        Text("📜 My Activity", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        if (myLogs.isEmpty()) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🍳", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text("No activity yet — go add a recipe!",
                    style     = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            myLogs.take(20).forEach { log -> ActivityItem(log) }
        }

        val mySubmissions = (recipeVM.pendingRecipes.collectAsState().value +
                recipeVM.rejectedRecipes.collectAsState().value)
            .filter { it.authorUid == user?.uid }

        if (mySubmissions.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            DoodleDivider(Modifier.fillMaxWidth().height(16.dp))
            Spacer(Modifier.height(16.dp))
            Text("📬 My Submissions", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            mySubmissions.forEach { recipe ->
                DoodleBorder(
                    modifier    = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    borderColor = when (recipe.status) {
                        "pending"  -> MaterialTheme.colorScheme.secondary
                        "rejected" -> Coral
                        else       -> MaterialTheme.colorScheme.primary
                    },
                    showCornerDots = false
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(recipe.title, style = MaterialTheme.typography.bodyLarge)
                            Text(recipe.category, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = when (recipe.status) {
                                "pending"  -> MaterialTheme.colorScheme.secondaryContainer
                                "rejected" -> Coral.copy(alpha = 0.15f)
                                else       -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = when (recipe.status) {
                                    "pending"  -> "⏳ Pending"
                                    "rejected" -> "❌ Rejected"
                                    else       -> "✅ Approved"
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = when (recipe.status) {
                                    "rejected" -> Coral
                                    else       -> MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Sign Out ─────────────────────────────────────────
        Spacer(Modifier.height(24.dp))
        DoodleDivider(Modifier.fillMaxWidth().height(16.dp))
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick  = { authVM.signOut() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out")
        }
        Spacer(Modifier.height(32.dp))
    }
}