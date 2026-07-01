package com.recipebook.community.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun WigglingFAB(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_wiggle")
    val angle by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "angle"
    )
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.rotate(angle).size(56.dp),
        containerColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Recipe")
    }
}
