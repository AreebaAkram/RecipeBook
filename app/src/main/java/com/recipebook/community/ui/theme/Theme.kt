package com.recipebook.community.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary        = BoldTeal,
    onPrimary      = WarmWhite,
    secondary      = Amber,
    onSecondary    = WarmWhite,
    tertiary       = SoftPurple,
    background     = WarmWhite,
    onBackground   = Color(0xFF1C1B1A),
    surface        = WarmWhite,
    onSurface      = Color(0xFF1C1B1A),
    error          = Coral
)

private val DarkColorScheme = darkColorScheme(
    primary        = DarkTeal,
    onPrimary      = DarkBg,
    secondary      = Amber,
    onSecondary    = DarkBg,
    tertiary       = SoftPurple,
    background     = DarkBg,
    onBackground   = Color(0xFFF0EDE6),
    surface        = DarkSurface,
    onSurface      = Color(0xFFF0EDE6),
    error          = Coral
)

@Composable
fun RecipeBookTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
