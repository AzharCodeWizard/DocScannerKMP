with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/viewmodel/CameraViewModel.kt', 'r') as f:
    content = f.read()

old_logic = """        if (_uiState.value.isAutoCaptureOn && _uiState.value.scanMode == ScanMode.DOCUMENT) {
            if (isStable) {
                val nextProgress = (_uiState.value.autoCaptureProgress + 0.06f).coerceAtMost(1.0f)
                _uiState.value = _uiState.value.copy(autoCaptureProgress = nextProgress)
            } else {
                _uiState.value = _uiState.value.copy(autoCaptureProgress = 0.0f)
            }
        } else {
            _uiState.value = _uiState.value.copy(autoCaptureProgress = 0.0f)
        }"""

new_logic = """        if (_uiState.value.isAutoCaptureOn && _uiState.value.scanMode == ScanMode.DOCUMENT) {
            if (isStable) {
                if (autoCaptureJob == null || !autoCaptureJob!!.isActive) {
                    startAutoCaptureTimer()
                }
            } else {
                cancelAutoCapture()
            }
        } else {
            cancelAutoCapture()
        }"""

content = content.replace(old_logic, new_logic)

# Replace start/cancel methods
old_cancel = """    private fun cancelAutoCapture() {
        autoCaptureJob?.cancel()
        _uiState.value = _uiState.value.copy(autoCaptureProgress = 0f)
    }"""

new_cancel = """    private fun startAutoCaptureTimer() {
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
    }"""

content = content.replace(old_cancel, new_cancel)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/viewmodel/CameraViewModel.kt', 'w') as f:
    f.write(content)
