package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    secondary = OrangeAccent,
    tertiary = PurpleGrey80,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for pro photo editor
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful dark slate Lightroom brand
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
