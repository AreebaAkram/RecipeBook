package com.recipebook.community.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DoodleBorder(
    modifier    : Modifier = Modifier,
    borderColor : Color    = Color(0xFF1D9E75),
    strokeWidth : Dp       = 2.5.dp,
    wobble      : Float    = 4f,
    showCornerDots: Boolean = true,
    content     : @Composable BoxScope.() -> Unit
) {
    val inf   = rememberInfiniteTransition(label = "doodle")
    val phase by inf.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "phase"
    )
    val dotPulse by inf.animateFloat(
        initialValue = 0.7f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse), label = "dot"
    )

    Box(modifier = modifier) {
        content()
        Canvas(modifier = Modifier.matchParentSize()) {
            val sw = strokeWidth.toPx()
            val path = Path()
            val w = size.width; val h = size.height
            val r = 28f; val pts = 80

            fun wobbly(t: Float, offset: Float) =
                sin((t * 5 + phase + offset) * Math.PI.toFloat()) * wobble

            // Top
            for (i in 0..pts) {
                val t = i / pts.toFloat()
                val x = r + t * (w - 2 * r)
                val y = sw + wobbly(t, 0f)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            // Right
            for (i in 0..pts) {
                val t = i / pts.toFloat()
                path.lineTo(w - sw + wobbly(t, 1f), r + t * (h - 2 * r))
            }
            // Bottom
            for (i in 0..pts) {
                val t = i / pts.toFloat()
                path.lineTo(w - r - t * (w - 2 * r), h - sw + wobbly(t, 2f))
            }
            // Left
            for (i in 0..pts) {
                val t = i / pts.toFloat()
                path.lineTo(sw + wobbly(t, 3f), h - r - t * (h - 2 * r))
            }
            path.close()
            drawPath(path, color = borderColor, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))

            if (showCornerDots) {
                val dotR = 5f * dotPulse
                val accent = Color(0xFFFF6B6B)
                val accent2 = Color(0xFF9B72CF)
                // Corner decorative dots
                drawCircle(accent,  dotR, Offset(20f, 20f))
                drawCircle(accent2, dotR * 0.8f, Offset(w - 20f, 20f))
                drawCircle(accent2, dotR * 0.9f, Offset(20f, h - 20f))
                drawCircle(accent,  dotR * 0.7f, Offset(w - 20f, h - 20f))

                // Small star at top-right corner
                drawStar(center = Offset(w - 18f, 18f), radius = 7f * dotPulse,
                    color = Color(0xFFEF9F27))
            }
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(
    center: Offset, radius: Float, color: Color, points: Int = 5
) {
    val path = Path()
    val innerR = radius * 0.45f
    for (i in 0 until points * 2) {
        val angle  = (i * Math.PI / points - Math.PI / 2).toFloat()
        val r      = if (i % 2 == 0) radius else innerR
        val x      = center.x + r * cos(angle)
        val y      = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}