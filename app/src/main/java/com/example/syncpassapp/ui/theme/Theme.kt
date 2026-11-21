package com.example.syncpassapp.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = PrimarySageLight,
    onPrimary = Color(0xFF0F3D2E),
    primaryContainer = PrimarySageDark,
    onPrimaryContainer = Color(0xFFE8F5E9),
    secondary = PrimarySage,
    onSecondary = Color.White,
    secondaryContainer = AccentMint,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentGold,
    onTertiary = Color(0xFF24332C),
    background = Color(0xFF121A16),
    onBackground = Color(0xFFE8F5E9),
    surface = Color(0xFF1A2420),
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF24332C),
    onSurfaceVariant = Color(0xFFB8D8CA),
    outline = Color(0xFF6B7E75),
    outlineVariant = Color(0xFF3B6E57)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimarySage,
    onPrimary = Color.White,
    primaryContainer = AccentMintLight,
    onPrimaryContainer = PrimarySageDark,
    secondary = PrimarySageLight,
    onSecondary = Color.White,
    secondaryContainer = AccentMint,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentGold,
    onTertiary = Color(0xFF24332C),
    background = BackgroundMint,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = AccentMintLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderSoft,
    outlineVariant = AccentMint
)

@Composable
fun SyncPassAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
