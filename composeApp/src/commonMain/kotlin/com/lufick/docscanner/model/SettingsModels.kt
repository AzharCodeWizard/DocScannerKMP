package com.lufick.docscanner.model

import androidx.compose.ui.graphics.Color
import com.lufick.docscanner.theme.*

enum class AppThemeMode(val displayName: String, val subtitle: String) {
    SYSTEM("System Default", "Follows device system settings"),
    DARK("Dark Theme", "Modern obsidian dark palette"),
    LIGHT("Light Theme", "Clean, high-contrast light theme"),
    AMOLED("AMOLED Pitch Black", "Pure #000000 blacks for maximum battery savings")
}

enum class AccentTheme(
    val displayName: String,
    val primaryColor: Color,
    val lightColor: Color,
    val darkColor: Color
) {
    EMERALD(
        displayName = "Emerald (Default)",
        primaryColor = Color(0xFF10B981),
        lightColor = Color(0xFF34D399),
        darkColor = Color(0xFF059669)
    ),
    CYAN(
        displayName = "Electric Cyan",
        primaryColor = Color(0xFF06B6D4),
        lightColor = Color(0xFF38BDF8),
        darkColor = Color(0xFF0891B2)
    ),
    VIOLET(
        displayName = "Royal Violet",
        primaryColor = Color(0xFF8B5CF6),
        lightColor = Color(0xFFA78BFA),
        darkColor = Color(0xFF7C3AED)
    ),
    SUNSET(
        displayName = "Sunset Amber",
        primaryColor = Color(0xFFF59E0B),
        lightColor = Color(0xFFFBBF24),
        darkColor = Color(0xFFD97706)
    ),
    SAPPHIRE(
        displayName = "Sapphire Blue",
        primaryColor = Color(0xFF3B82F6),
        lightColor = Color(0xFF60A5FA),
        darkColor = Color(0xFF2563EB)
    ),
    CRIMSON(
        displayName = "Rose Crimson",
        primaryColor = Color(0xFFF43F5E),
        lightColor = Color(0xFFFB7185),
        darkColor = Color(0xFFE11D48)
    )
}

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val accentTheme: AccentTheme = AccentTheme.EMERALD,
    val defaultQuality: PdfQuality = PdfQuality.HIGH,
    val defaultPageSize: PageSize = PageSize.A4,
    val autoSaveToGallery: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val defaultWatermarkText: String = "DocScanner Confidential",
    val isWatermarkEnabledByDefault: Boolean = false,
    val ocrLanguage: String = "English (Latin)",
    val storageUsedMb: Float = 14.8f
)
