package com.lufick.docscanner.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lufick.docscanner.model.AccentTheme
import com.lufick.docscanner.model.AppThemeMode

fun getDynamicColorScheme(
    themeMode: AppThemeMode,
    accentTheme: AccentTheme,
    isSystemDark: Boolean
): ColorScheme {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.AMOLED -> true
    }

    val primary = accentTheme.primaryColor
    val primaryDark = accentTheme.darkColor
    val primaryLight = accentTheme.lightColor

    if (themeMode == AppThemeMode.AMOLED) {
        return darkColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = primaryDark,
            onPrimaryContainer = Color.White,
            secondary = primaryLight,
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF121212),
            onSecondaryContainer = Color.White,
            background = Color.Black,
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF0A0A0A),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF141414),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF262626)
        )
    }

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = DarkBackground,
            primaryContainer = primaryDark,
            onPrimaryContainer = DarkTextPrimary,
            secondary = primaryLight,
            onSecondary = DarkBackground,
            secondaryContainer = DarkSurfaceVariant,
            onSecondaryContainer = DarkTextPrimary,
            background = DarkBackground,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkBorder
        )
    } else {
        lightColorScheme(
            primary = primaryDark,
            onPrimary = LightSurface,
            primaryContainer = primaryLight,
            onPrimaryContainer = LightTextPrimary,
            secondary = primary,
            onSecondary = LightSurface,
            secondaryContainer = LightSurfaceVariant,
            onSecondaryContainer = LightTextPrimary,
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightTextSecondary,
            outline = LightBorder
        )
    }
}

@Composable
fun DocScannerTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.EMERALD,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = getDynamicColorScheme(themeMode, accentTheme, isSystemDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DocScannerTypography,
        content = content
    )
}
