package com.recipebook.community.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "shimmer_x"
    )
    return Brush.linearGradient(
        colors = listOf(Color(0xFFE8E3D8), Color(0xFFF5F0E8), Color(0xFFE8E3D8)),
        start  = Offset(translateX, 0f),
        end    = Offset(translateX + 300f, 300f)
    )
}

@Composable
fun RecipeCardSkeleton() {
    val brush = ShimmerBrush()
    Column(Modifier.padding(16.dp).fillMaxWidth()) {
        repeat(3) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Box(Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)).background(brush))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth(0.7f).height(18.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(0.4f).height(14.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                }
            }
        }
    }
}
