package com.recipebook.community.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun DoodleDivider(modifier: Modifier = Modifier, color: Color = Color(0xFFDDD8CC)) {
    Canvas(modifier = modifier) {
        val path  = Path()
        val steps = 100
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val x = t * size.width
            val y = size.height / 2 + sin(t * 7 * Math.PI.toFloat()) * 4f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        // Decorative dots along divider
        val dotColors = listOf(Color(0xFFFF6B6B), Color(0xFF1D9E75), Color(0xFFEF9F27), Color(0xFF9B72CF))
        for (i in 1..4) {
            val x = size.width * (i / 5f)
            drawCircle(dotColors[(i - 1) % dotColors.size], radius = 4f, center = Offset(x, size.height / 2))
        }
    }
}