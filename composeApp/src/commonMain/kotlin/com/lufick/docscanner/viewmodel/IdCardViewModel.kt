package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class IdCardSide { FRONT, BACK, PREVIEW }

data class IdCardUiState(
    val currentSide: IdCardSide = IdCardSide.FRONT,
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val stitchedImagePath: String? = null
)

class IdCardViewModel : ViewModel() {

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
    }

    fun retake() {
        _uiState.value = IdCardUiState()
    }
}
