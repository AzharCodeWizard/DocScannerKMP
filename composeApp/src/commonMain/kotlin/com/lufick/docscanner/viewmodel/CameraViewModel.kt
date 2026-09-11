package com.lufick.docscanner.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.model.ScanMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FlashMode(val title: String) {
    OFF("Off"),
    ON("On"),
    AUTO("Auto"),
    TORCH("Torch")
}

enum class DetectionState(val message: String) {
    SEARCHING("Looking for document..."),
    DETECTED("Document detected"),
    HOLD_STILL("Hold still... capturing"),
    CAPTURING("Capturing page...")
}

data class CameraUiState(
    val scanMode: ScanMode = ScanMode.DOCUMENT,
    val isBatchMode: Boolean = false,
    val flashMode: FlashMode = FlashMode.OFF,
    val isAutoCaptureOn: Boolean = false,
    val isGridVisible: Boolean = false,
    val isSpiritLevelVisible: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val detectionState: DetectionState = DetectionState.DETECTED,
    val autoCaptureProgress: Float = 0.0f,
    val detectedQuad: QuadCorners = QuadCorners(
        topLeft = PointF(0.08f, 0.12f),
        topRight = PointF(0.92f, 0.12f),
        bottomRight = PointF(0.92f, 0.62f),
        bottomLeft = PointF(0.08f, 0.62f)
    ),
    val isIdCardFront: Boolean = true,
    val tapFocusPoint: Offset? = null,
    val capturedImages: List<String> = emptyList()
) {
    val batchCount: Int get() = capturedImages.size
}

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var autoCaptureJob: Job? = null
    private var focusResetJob: Job? = null
    private var stableFrameCount = 0

    fun setScanMode(mode: ScanMode) {
        _uiState.value = _uiState.value.copy(scanMode = mode)
        stableFrameCount = 0
        cancelAutoCapture()
    }

    fun cycleFlashMode() {
        val nextMode = when (_uiState.value.flashMode) {
            FlashMode.OFF -> FlashMode.ON
            FlashMode.ON -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.TORCH
            FlashMode.TORCH -> FlashMode.OFF
        }
        _uiState.value = _uiState.value.copy(flashMode = nextMode)
    }

    fun toggleBatchMode() {
        _uiState.value = _uiState.value.copy(isBatchMode = !_uiState.value.isBatchMode)
    }

    fun toggleAutoCapture() {
        val newAuto = !_uiState.value.isAutoCaptureOn
        _uiState.value = _uiState.value.copy(isAutoCaptureOn = newAuto)
        if (!newAuto) {
            cancelAutoCapture()
        }
    }

    fun toggleGrid() {
        _uiState.value = _uiState.value.copy(isGridVisible = !_uiState.value.isGridVisible)
    }

    fun toggleSpiritLevel() {
        _uiState.value = _uiState.value.copy(isSpiritLevelVisible = !_uiState.value.isSpiritLevelVisible)
    }

    fun setZoom(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoomRatio = zoom)
    }

    fun onScreenTapped(point: Offset) {
        _uiState.value = _uiState.value.copy(tapFocusPoint = point)
        focusResetJob?.cancel()
        focusResetJob = viewModelScope.launch {
            delay(1800)
            _uiState.value = _uiState.value.copy(tapFocusPoint = null)
        }
    }

    fun onEdgeDetected(quad: QuadCorners) {
        val curr = _uiState.value.detectedQuad

        val moveTl = kotlin.math.hypot((quad.topLeft.x - curr.topLeft.x).toDouble(), (quad.topLeft.y - curr.topLeft.y).toDouble()).toFloat()
        val moveTr = kotlin.math.hypot((quad.topRight.x - curr.topRight.x).toDouble(), (quad.topRight.y - curr.topRight.y).toDouble()).toFloat()
        val moveBr = kotlin.math.hypot((quad.bottomRight.x - curr.bottomRight.x).toDouble(), (quad.bottomRight.y - curr.bottomRight.y).toDouble()).toFloat()
        val moveBl = kotlin.math.hypot((quad.bottomLeft.x - curr.bottomLeft.x).toDouble(), (quad.bottomLeft.y - curr.bottomLeft.y).toDouble()).toFloat()
        val maxPointMovement = maxOf(moveTl, moveTr, moveBr, moveBl)

        val isMovementMinimal = maxPointMovement < 0.025f

        if (isMovementMinimal) {
            stableFrameCount++
        } else {
            stableFrameCount = 0
            cancelAutoCapture()
        }

        // Requires 3 consecutive stable frames before triggering HOLD_STILL
        val isSteady = stableFrameCount >= 3
        val newState = if (isSteady) DetectionState.HOLD_STILL else DetectionState.DETECTED

        // Only update quad when movement is outside deadband (> 0.015f)
        val updatedQuad = if (maxPointMovement >= 0.015f) {
            val lerpFactor = if (maxPointMovement > 0.10f) 0.35f else 0.22f
            QuadCorners(
                topLeft = PointF(
                    curr.topLeft.x + (quad.topLeft.x - curr.topLeft.x) * lerpFactor,
                    curr.topLeft.y + (quad.topLeft.y - curr.topLeft.y) * lerpFactor
                ),
                topRight = PointF(
                    curr.topRight.x + (quad.topRight.x - curr.topRight.x) * lerpFactor,
                    curr.topRight.y + (quad.topRight.y - curr.topRight.y) * lerpFactor
                ),
                bottomRight = PointF(
                    curr.bottomRight.x + (quad.bottomRight.x - curr.bottomRight.x) * lerpFactor,
                    curr.bottomRight.y + (quad.bottomRight.y - curr.bottomRight.y) * lerpFactor
                ),
                bottomLeft = PointF(
                    curr.bottomLeft.x + (quad.bottomLeft.x - curr.bottomLeft.x) * lerpFactor,
                    curr.bottomLeft.y + (quad.bottomLeft.y - curr.bottomLeft.y) * lerpFactor
                )
            )
        } else {
            curr
        }

        if (updatedQuad != curr || newState != _uiState.value.detectionState) {
            _uiState.value = _uiState.value.copy(
                detectedQuad = updatedQuad,
                detectionState = newState
            )
        }

        if (_uiState.value.isAutoCaptureOn && _uiState.value.scanMode == ScanMode.DOCUMENT) {
            if (isSteady) {
                if (autoCaptureJob == null || !autoCaptureJob!!.isActive) {
                    startAutoCaptureTimer()
                }
            }
        }
    }

    fun onPhotoCaptured(path: String) {
        val current = _uiState.value.capturedImages
        _uiState.value = _uiState.value.copy(
            capturedImages = current + path,
            autoCaptureProgress = 0f,
            detectionState = DetectionState.DETECTED
        )
    }

    fun removeCapturedPage(index: Int) {
        val current = _uiState.value.capturedImages.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(capturedImages = current)
        }
    }

    fun toggleIdCardSide() {
        _uiState.value = _uiState.value.copy(isIdCardFront = !_uiState.value.isIdCardFront)
    }

    private fun startAutoCaptureTimer() {
        autoCaptureJob?.cancel()
        autoCaptureJob = viewModelScope.launch {
            val steps = 20
            for (i in 1..steps) {
                delay(45L) // ~900ms smooth countdown
                _uiState.value = _uiState.value.copy(
                    autoCaptureProgress = i.toFloat() / steps.toFloat(),
                    detectionState = DetectionState.HOLD_STILL
                )
            }
        }
    }

    private fun cancelAutoCapture() {
        autoCaptureJob?.cancel()
        autoCaptureJob = null
        if (_uiState.value.autoCaptureProgress != 0f) {
            _uiState.value = _uiState.value.copy(autoCaptureProgress = 0f)
        }
    }
}
