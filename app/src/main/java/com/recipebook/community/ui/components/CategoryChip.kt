package com.recipebook.community.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.recipebook.community.ui.theme.categoryColor

@Composable
fun CategoryChip(category: String, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    val color = categoryColor(category)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) color else color.copy(alpha = 0.12f))
            .border(1.5.dp, color, RoundedCornerShape(50))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(category, color = if (selected) Color.White else color, fontSize = 12.sp)
    }
}
