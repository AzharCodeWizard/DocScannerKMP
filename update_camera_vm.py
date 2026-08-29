with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/viewmodel/CameraViewModel.kt', 'r') as f:
    content = f.read()

# Replace detectedQuad default
old_quad = """    val detectedQuad: QuadCorners = QuadCorners(
        topLeft = PointF(0.10f, 0.14f),
        topRight = PointF(0.90f, 0.14f),
        bottomRight = PointF(0.88f, 0.86f),
        bottomLeft = PointF(0.12f, 0.86f)
    ),"""

new_quad = """    val detectedQuad: QuadCorners = QuadCorners(
        topLeft = PointF(0.08f, 0.12f),
        topRight = PointF(0.92f, 0.12f),
        bottomRight = PointF(0.92f, 0.62f),
        bottomLeft = PointF(0.08f, 0.62f)
    ),"""

content = content.replace(old_quad, new_quad)

# Update onEdgeDetected to handle auto-capture progression and smooth lerp
old_on_edge = """    fun onEdgeDetected(quad: QuadCorners) {
        // Smooth lerp update
        val curr = _uiState.value.detectedQuad
        val lerpFactor = 0.4f
        val smoothed = QuadCorners(
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
        _uiState.value = _uiState.value.copy(
            detectedQuad = smoothed,
            detectionState = DetectionState.DETECTED
        )
    }"""

new_on_edge = """    fun onEdgeDetected(quad: QuadCorners) {
        // Smooth lerp update
        val curr = _uiState.value.detectedQuad
        val lerpFactor = 0.35f
        val smoothed = QuadCorners(
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

        val isStable = kotlin.math.abs(curr.topLeft.x - quad.topLeft.x) < 0.05f &&
                       kotlin.math.abs(curr.topLeft.y - quad.topLeft.y) < 0.05f

        val newDetectionState = if (isStable) DetectionState.HOLD_STILL else DetectionState.DETECTED

        _uiState.value = _uiState.value.copy(
            detectedQuad = smoothed,
            detectionState = newDetectionState
        )

        if (_uiState.value.isAutoCaptureOn && _uiState.value.scanMode == ScanMode.DOCUMENT) {
            if (isStable) {
                val nextProgress = (_uiState.value.autoCaptureProgress + 0.06f).coerceAtMost(1.0f)
                _uiState.value = _uiState.value.copy(autoCaptureProgress = nextProgress)
            } else {
                _uiState.value = _uiState.value.copy(autoCaptureProgress = 0.0f)
            }
        } else {
            _uiState.value = _uiState.value.copy(autoCaptureProgress = 0.0f)
        }
    }"""

content = content.replace(old_on_edge, new_on_edge)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/viewmodel/CameraViewModel.kt', 'w') as f:
    f.write(content)
