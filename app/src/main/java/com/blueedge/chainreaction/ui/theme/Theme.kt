package com.blueedge.chainreaction.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val fontFamily = fontFamilyForAppFont(GameConfig.appFont)
    val typography = createTypography(fontFamily)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}