package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MutedGold,
    secondary = DeepNavy,
    background = ParchmentDark,
    surface = ParchmentDarkCard,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText
)

private val LightColorScheme = lightColorScheme(
    primary = MutedGold,
    secondary = DeepNavy,
    background = ParchmentLight,
    surface = ParchmentCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = CharcoalText,
    onSurface = CharcoalText
)

@Composable
fun WorldLiteratureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
