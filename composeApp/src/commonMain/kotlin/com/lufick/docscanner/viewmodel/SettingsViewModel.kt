package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import com.lufick.docscanner.model.AccentTheme
import com.lufick.docscanner.model.AppThemeMode
import com.lufick.docscanner.model.PageSize
import com.lufick.docscanner.model.PdfQuality
import com.lufick.docscanner.model.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun setAccentTheme(accent: AccentTheme) {
        _uiState.value = _uiState.value.copy(accentTheme = accent)
    }

    fun setDefaultQuality(quality: PdfQuality) {
        _uiState.value = _uiState.value.copy(defaultQuality = quality)
    }

    fun setDefaultPageSize(pageSize: PageSize) {
        _uiState.value = _uiState.value.copy(defaultPageSize = pageSize)
    }

    fun toggleAutoSaveToGallery() {
        _uiState.value = _uiState.value.copy(autoSaveToGallery = !_uiState.value.autoSaveToGallery)
    }

    fun toggleHapticFeedback() {
        _uiState.value = _uiState.value.copy(hapticFeedbackEnabled = !_uiState.value.hapticFeedbackEnabled)
    }

    fun updateDefaultWatermark(text: String, isEnabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            defaultWatermarkText = text,
            isWatermarkEnabledByDefault = isEnabled
        )
    }

    fun setOcrLanguage(language: String) {
        _uiState.value = _uiState.value.copy(ocrLanguage = language)
    }

    fun clearCache() {
        _uiState.value = _uiState.value.copy(storageUsedMb = 0.4f)
    }
}
