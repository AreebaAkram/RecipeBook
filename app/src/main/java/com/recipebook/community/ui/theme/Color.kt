package com.recipebook.community.ui.theme

import androidx.compose.ui.graphics.Color

val WarmWhite     = Color(0xFFFFFDF7)
val BoldTeal      = Color(0xFF1D9E75)
val Amber         = Color(0xFFEF9F27)
val Coral         = Color(0xFFFF6B6B)
val SoftPurple    = Color(0xFF9B72CF)
val DarkBg        = Color(0xFF1C1B1A)
val DarkSurface   = Color(0xFF2A2825)
val DarkTeal      = Color(0xFF25C48F)

// Category colors
val BreakfastColor = Amber
val LunchColor     = BoldTeal
val DinnerColor    = Coral
val SnacksColor    = SoftPurple
val DessertsColor  = Color(0xFFE07BBD)
val DrinksColor    = Color(0xFF5BB8E8)

fun categoryColor(category: String): Color = when (category) {
    "Breakfast" -> BreakfastColor
    "Lunch"     -> LunchColor
    "Dinner"    -> DinnerColor
    "Snacks"    -> SnacksColor
    "Desserts"  -> DessertsColor
    "Drinks"    -> DrinksColor
    else        -> BoldTeal
}
