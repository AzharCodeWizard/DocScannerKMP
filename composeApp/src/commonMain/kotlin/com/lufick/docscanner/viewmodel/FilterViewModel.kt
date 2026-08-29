package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import com.lufick.docscanner.engine.DocumentTemplateType
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.QuadCorners
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FilterUiState(
    val imagePath: String = "",
    val templateType: DocumentTemplateType = DocumentTemplateType.RECEIPT,
    val selectedFilter: FilterType = FilterType.MAGIC_COLOR_1,
    val brightness: Float = 1.0f,
    val contrast: Float = 1.25f,
    val rotationDegrees: Int = 0,
    val corners: QuadCorners = QuadCorners(),
    val isComparisonMode: Boolean = false
)

class FilterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    fun setImage(
        path: String,
        template: DocumentTemplateType = DocumentTemplateType.RECEIPT,
        corners: QuadCorners = QuadCorners(),
        rotation: Int = 0
    ) {
        _uiState.value = _uiState.value.copy(
            imagePath = path,
            templateType = template,
            corners = corners,
            rotationDegrees = rotation,
            selectedFilter = FilterType.MAGIC_COLOR_1
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

    fun toggleComparison() {
        _uiState.value = _uiState.value.copy(isComparisonMode = !_uiState.value.isComparisonMode)
    }
}
