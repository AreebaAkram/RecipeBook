package com.recipebook.community.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class AvatarOption(val label: String, val seed: String, val style: String)

val AVATAR_OPTIONS = listOf(
    AvatarOption("👨 Alex",    "Alex",    "adventurer"),
    AvatarOption("👩 Mia",     "Mia",     "adventurer"),
    AvatarOption("🧔 Omar",    "Omar",    "adventurer"),
    AvatarOption("👱‍♀️ Sofia",  "Sofia",   "adventurer"),
    AvatarOption("🤖 R2-D9",   "robot9",  "bottts"),
    AvatarOption("🤖 Zap",     "zap42",   "bottts"),
    AvatarOption("🧑 Jordan",  "Jordan",  "adventurer"),
    AvatarOption("👩‍🦱 Priya",  "Priya",   "adventurer"),
    AvatarOption("👨‍🦲 Marcus", "Marcus",  "adventurer"),
    AvatarOption("🧝‍♀️ Luna",   "Luna",    "adventurer")
)

@Composable
fun AvatarView(
    name        : String,
    colorHex    : String,
    size        : Dp       = 40.dp,
    modifier    : Modifier = Modifier,
    avatarSeed  : String   = "",
    avatarStyle : String   = "adventurer"
) {
    val seed  = avatarSeed.ifEmpty { name.replace(" ", "+") }
    val bg    = colorHex.removePrefix("#")
    // Use PNG endpoint instead of SVG — Coil handles PNG natively without extra decoder
    val url   = "https://api.dicebear.com/8.x/$avatarStyle/png" +
            "?seed=$seed&backgroundColor=$bg&size=128"
    val color = runCatching {
        Color(android.graphics.Color.parseColor(
            if (colorHex.startsWith("#")) colorHex else "#1D9E75"))
    }.getOrElse { Color(0xFF1D9E75) }

    val ctx = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(ctx)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = name,
        contentScale       = ContentScale.Crop,
        modifier           = modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, color, CircleShape)
    )
}