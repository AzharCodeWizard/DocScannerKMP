package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.QuadCorners
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FilterTab(val title: String) {
    PRESETS("Filters"),
    ADJUST("Adjust"),
    TOOLS("Tools")
}

data class FilterUiState(
    val imagePath: String = "",
    val selectedFilter: FilterType = FilterType.MAGIC_COLOR_1,
    val brightness: Float = 1.0f,
    val contrast: Float = 1.25f,
    val saturation: Float = 1.0f,
    val rotationDegrees: Int = 0,
    val corners: QuadCorners = QuadCorners(),
    val isComparisonMode: Boolean = false,
    val activeTab: FilterTab = FilterTab.PRESETS
)

class FilterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    fun setImage(
        path: String,
        corners: QuadCorners = QuadCorners(),
        rotation: Int = 0
    ) {
        _uiState.value = _uiState.value.copy(
            imagePath = path,
            corners = corners,
            rotationDegrees = rotation,
            selectedFilter = FilterType.MAGIC_COLOR_1,
            brightness = 1.0f,
            contrast = 1.25f,
            saturation = 1.0f,
            activeTab = FilterTab.PRESETS
        )
    }

    fun selectFilter(filter: FilterType) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun setBrightness(value: Float) {
        _uiState.value = _uiState.value.copy(brightness = value)
    }

    fun setContrast(value: Float) {
        _uiState.value = _uiState.value.copy(contrast = value)
    }

    fun setSaturation(value: Float) {
        _uiState.value = _uiState.value.copy(saturation = value)
    }

    fun resetBrightness() {
        _uiState.value = _uiState.value.copy(brightness = 1.0f)
    }

    fun resetContrast() {
        _uiState.value = _uiState.value.copy(contrast = 1.25f)
    }

    fun resetSaturation() {
        _uiState.value = _uiState.value.copy(saturation = 1.0f)
    }

    fun setActiveTab(tab: FilterTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun rotate90() {
        val next = (_uiState.value.rotationDegrees + 90) % 360
        _uiState.value = _uiState.value.copy(rotationDegrees = next)
    }

    fun rotateCounterClockwise() {
        val next = (_uiState.value.rotationDegrees + 270) % 360
        _uiState.value = _uiState.value.copy(rotationDegrees = next)
    }

    fun autoEnhance() {
        _uiState.value = _uiState.value.copy(
            selectedFilter = FilterType.MAGIC_COLOR_1,
            brightness = 1.05f,
            contrast = 1.35f,
            saturation = 1.18f
        )
    }

    fun resetAll() {
        _uiState.value = _uiState.value.copy(
            selectedFilter = FilterType.ORIGINAL,
            brightness = 1.0f,
            contrast = 1.20f,
            saturation = 1.0f,
            rotationDegrees = 0
        )
    }

    fun setComparisonMode(active: Boolean) {
        _uiState.value = _uiState.value.copy(isComparisonMode = active)
    }

    fun toggleComparison() {
        _uiState.value = _uiState.value.copy(isComparisonMode = !_uiState.value.isComparisonMode)
    }
}

