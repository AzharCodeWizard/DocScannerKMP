with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'r') as f:
    content = f.read()

old_trigger = """    // Auto-Capture Shutter Trigger
    LaunchedEffect(uiState.autoCaptureProgress) {
        if (uiState.autoCaptureProgress >= 1.0f && uiState.isAutoCaptureOn) {
            cameraHandler?.capturePhoto { capturedPath ->
                viewModel.onPhotoCaptured(capturedPath)
                if (!uiState.isBatchMode) {
                    onNavigateToCrop()
                }
            }
        }
    }"""

new_trigger = """    var isCapturing by remember { mutableStateOf(false) }

    // Auto-Capture Shutter Trigger (with single-shot guard)
    LaunchedEffect(uiState.autoCaptureProgress) {
        if (uiState.autoCaptureProgress >= 1.0f && uiState.isAutoCaptureOn && !isCapturing) {
            isCapturing = true
            cameraHandler?.capturePhoto { capturedPath ->
                viewModel.onPhotoCaptured(capturedPath)
                isCapturing = false
                if (!uiState.isBatchMode) {
                    onNavigateToCrop()
                }
            }
        }
    }"""

content = content.replace(old_trigger, new_trigger)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'w') as f:
    f.write(content)
