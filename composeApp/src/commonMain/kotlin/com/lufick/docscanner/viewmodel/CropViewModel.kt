package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import com.lufick.docscanner.engine.DocumentTemplateType
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.platform.PlatformImageProcessor
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CropAspectRatio(val displayName: String, val ratio: Float?) {
    FREE("Free", null),
    A4("A4 (1:1.41)", 1f / 1.414f),
    LETTER("Letter", 8.5f / 11f),
    ID_CARD("ID Card (3:2)", 3f / 2f),
    SQUARE("1:1", 1f)
}

data class CropUiState(
    val imagePath: String = "",
    val templateType: DocumentTemplateType = DocumentTemplateType.RECEIPT,
    val corners: QuadCorners = QuadCorners(
        topLeft = PointF(0.06f, 0.08f),
        topRight = PointF(0.94f, 0.08f),
        bottomRight = PointF(0.94f, 0.92f),
        bottomLeft = PointF(0.06f, 0.92f)
    ),
    val activeCornerIndex: Int? = null,
    val rotationDegrees: Int = 0,
    val selectedAspectRatio: CropAspectRatio = CropAspectRatio.FREE
)

class CropViewModel(private val imageProcessor: PlatformImageProcessor? = null) : ViewModel() {

    private val _uiState = MutableStateFlow(CropUiState())
    val uiState: StateFlow<CropUiState> = _uiState.asStateFlow()

    fun setImage(
        path: String,
        template: DocumentTemplateType = DocumentTemplateType.RECEIPT,
        initialCorners: QuadCorners? = null
    ) {
        val defaultQuad = initialCorners ?: QuadCorners(
            topLeft = PointF(0.06f, 0.08f),
            topRight = PointF(0.94f, 0.08f),
            bottomRight = PointF(0.94f, 0.92f),
            bottomLeft = PointF(0.06f, 0.92f)
        )
        _uiState.value = _uiState.value.copy(
            imagePath = path,
            templateType = template,
            rotationDegrees = 0,
            corners = defaultQuad
        )

        // If no initial corners provided from camera, run auto-detection on the captured image
        if (initialCorners == null && imageProcessor != null && path.isNotEmpty()) {
            autoDetect()
        }
    }

    fun updateCorners(corners: QuadCorners) {
        _uiState.value = _uiState.value.copy(corners = corners)
    }

    fun updateCorner(cornerIndex: Int, newPos: PointF) {
        val current = _uiState.value.corners
        val updated = when (cornerIndex) {
            0 -> current.copy(topLeft = newPos)
            1 -> current.copy(topRight = newPos)
            2 -> current.copy(bottomRight = newPos)
            3 -> current.copy(bottomLeft = newPos)
            else -> current
        }
        _uiState.value = _uiState.value.copy(
            corners = updated,
            activeCornerIndex = cornerIndex
        )
    }

    fun finishCornerDrag() {
        _uiState.value = _uiState.value.copy(activeCornerIndex = null)
    }

    fun rotate90() {
        val newRot = (_uiState.value.rotationDegrees + 90) % 360
        _uiState.value = _uiState.value.copy(rotationDegrees = newRot)
    }

    fun autoDetect() {
        val currentPath = _uiState.value.imagePath
        if (imageProcessor != null && currentPath.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val detected = imageProcessor.detectDocumentCorners(currentPath)
                    _uiState.value = _uiState.value.copy(corners = detected)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        corners = QuadCorners(
                            topLeft = PointF(0.08f, 0.08f),
                            topRight = PointF(0.92f, 0.08f),
                            bottomRight = PointF(0.92f, 0.92f),
                            bottomLeft = PointF(0.08f, 0.92f)
                        )
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                corners = QuadCorners(
                    topLeft = PointF(0.08f, 0.08f),
                    topRight = PointF(0.92f, 0.08f),
                    bottomRight = PointF(0.92f, 0.92f),
                    bottomLeft = PointF(0.08f, 0.92f)
                )
            )
        }
    }

    fun fullPage() {
        _uiState.value = _uiState.value.copy(
            corners = QuadCorners(
                topLeft = PointF(0f, 0f),
                topRight = PointF(1f, 0f),
                bottomRight = PointF(1f, 1f),
                bottomLeft = PointF(0f, 1f)
            )
        )
    }

    fun setAspectRatio(aspectRatio: CropAspectRatio) {
        _uiState.value = _uiState.value.copy(selectedAspectRatio = aspectRatio)
        val r = aspectRatio.ratio
        if (r != null) {
            // Apply proportional quad
            val marginX = 0.08f
            val width = 1.0f - 2 * marginX
            val height = (width / r).coerceAtMost(0.85f)
            val marginY = (1.0f - height) / 2f

            _uiState.value = _uiState.value.copy(
                corners = QuadCorners(
                    topLeft = PointF(marginX, marginY),
                    topRight = PointF(marginX + width, marginY),
                    bottomRight = PointF(marginX + width, marginY + height),
                    bottomLeft = PointF(marginX, marginY + height)
                )
            )
        }
    }
}
