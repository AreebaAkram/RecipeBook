package com.recipebook.community.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun DoodleBorder(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF1D9E75),
    strokeWidth: Dp = 2.5.dp,
    wobble: Float = 4f,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "doodle")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "phase"
    )

    Box(modifier = modifier) {
        content()
        Canvas(modifier = Modifier.matchParentSize()) {
            val sw = strokeWidth.toPx()
            val path = Path()
            val w = size.width; val h = size.height
            val r = 24f
            val pts = 80

            // Top edge
            for (i in 0..pts) {
                val t  = i / pts.toFloat()
                val x  = r + t * (w - 2 * r)
                val y  = sw + sin((t * 4 + phase) * Math.PI.toFloat()) * wobble
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            // Right edge
            for (i in 0..pts) {
                val t  = i / pts.toFloat()
                val x  = w - sw + sin((t * 4 + phase) * Math.PI.toFloat()) * wobble
                val y  = r + t * (h - 2 * r)
                path.lineTo(x, y)
            }
            // Bottom edge
            for (i in 0..pts) {
                val t  = i / pts.toFloat()
                val x  = w - r - t * (w - 2 * r)
                val y  = h - sw + sin((t * 4 + phase) * Math.PI.toFloat()) * wobble
                path.lineTo(x, y)
            }
            // Left edge
            for (i in 0..pts) {
                val t  = i / pts.toFloat()
                val x  = sw + sin((t * 4 + phase) * Math.PI.toFloat()) * wobble
                val y  = h - r - t * (h - 2 * r)
                path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = borderColor, style = Stroke(width = sw, cap = StrokeCap.Round))
        }
    }
}
