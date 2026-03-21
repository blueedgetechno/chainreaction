package com.blueedge.chainreaction.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.blueedge.chainreaction.data.GameConfig

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF41AFD4),
    secondary = Color(0xFFEA695E),
    tertiary = Color(0xFFEA695E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF41AFD4),
    secondary = Color(0xFFEA695E),
    tertiary = Color(0xFFEA695E),
    primaryContainer = Color(0xFFD6F0FA),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

@Composable
fun ChainReactionTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val fontFamily = fontFamilyForAppFont(GameConfig.appFont)
    val typography = createTypography(fontFamily)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
