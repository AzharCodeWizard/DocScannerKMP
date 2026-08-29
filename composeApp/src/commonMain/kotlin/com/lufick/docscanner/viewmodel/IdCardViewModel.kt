package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.lufick.docscanner.platform.PlatformImageProcessor
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

enum class IdCardSide { FRONT, BACK, PREVIEW }

data class IdCardUiState(
    val currentSide: IdCardSide = IdCardSide.FRONT,
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val stitchedImagePath: String? = null
)

class IdCardViewModel(private val imageProcessor: PlatformImageProcessor) : ViewModel() {

    private val _uiState = MutableStateFlow(IdCardUiState())
    val uiState: StateFlow<IdCardUiState> = _uiState.asStateFlow()

    fun onFrontCaptured(path: String) {
        _uiState.value = _uiState.value.copy(
            frontImagePath = path,
            currentSide = IdCardSide.BACK
        )
    }

    fun onBackCaptured(path: String) {
        _uiState.value = _uiState.value.copy(
            backImagePath = path,
            currentSide = IdCardSide.PREVIEW
        )
        // Trigger stitch
        viewModelScope.launch {
            val front = _uiState.value.frontImagePath
            val back = path
            if (front != null) {
                val stitched = imageProcessor.stitchIdCard(front, back)
                _uiState.value = _uiState.value.copy(stitchedImagePath = stitched)
            }
        }
    }

    fun retake() {
        _uiState.value = IdCardUiState()
    }
}
