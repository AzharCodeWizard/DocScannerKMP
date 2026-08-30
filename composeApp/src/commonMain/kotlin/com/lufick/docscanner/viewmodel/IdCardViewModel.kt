package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lufick.docscanner.model.Document
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.platform.PlatformImageProcessor
import com.lufick.docscanner.repository.DocumentRepository
import com.lufick.docscanner.util.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class IdCardSide { FRONT, BACK, PREVIEW }

data class IdCardUiState(
    val currentSide: IdCardSide = IdCardSide.FRONT,
    val frontImagePath: String? = null,
    val backImagePath: String? = null,
    val stitchedImagePath: String? = null,
    val isSaving: Boolean = false,
    val isStitching: Boolean = false
)

class IdCardViewModel(
    private val imageProcessor: PlatformImageProcessor,
    private val repository: DocumentRepository? = null
) : ViewModel() {

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
            currentSide = IdCardSide.PREVIEW,
            isStitching = true
        )
        // Trigger stitch
        viewModelScope.launch {
            val front = _uiState.value.frontImagePath
            val back = path
            if (front != null) {
                val stitched = imageProcessor.stitchIdCard(front, back)
                _uiState.value = _uiState.value.copy(
                    stitchedImagePath = stitched,
                    isStitching = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isStitching = false)
            }
        }
    }

    fun saveCard(title: String, onSaved: (String) -> Unit) {
        val stitched = _uiState.value.stitchedImagePath ?: return
        val repo = repository ?: return
        _uiState.value = _uiState.value.copy(isSaving = true)

        viewModelScope.launch {
            val now = currentTimeMillis()
            val newDocId = "id_card_$now"
            val page = ScannedPage(
                id = "p_$now",
                pageNumber = 1,
                originalImagePath = stitched,
                processedImagePath = stitched,
                cropCorners = QuadCorners(
                    topLeft = PointF(0f, 0f),
                    topRight = PointF(1f, 0f),
                    bottomRight = PointF(1f, 1f),
                    bottomLeft = PointF(0f, 1f)
                ),
                rotationDegrees = 0,
                filterType = FilterType.ORIGINAL,
                brightness = 0f,
                contrast = 1f,
                ocrText = "GOVERNMENT OF INDIA / ID CARD\nFront & Back Stitched",
                createdAt = now
            )

            val doc = Document(
                id = newDocId,
                title = title.ifBlank { "National ID Card" },
                folderId = "f_id_cards",
                tags = listOf("ID Card", "2-in-1", "A4 Sheet"),
                pages = listOf(page),
                createdAt = now,
                updatedAt = now,
                isFavorite = true
            )

            repo.saveDocument(doc)
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSaved(newDocId)
        }
    }

    fun retake() {
        _uiState.value = IdCardUiState()
    }
}
