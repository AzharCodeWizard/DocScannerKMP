import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.lufick.docscanner.platform.CameraPreview", "import com.lufick.docscanner.platform.CameraPreview\nimport com.lufick.docscanner.platform.PlatformCameraHandler")

preview_old = """                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    flashEnabled = uiState.flashMode != FlashMode.OFF,
                    onEdgeDetected = { quad -> viewModel.onEdgeDetected(quad) }
                )"""

preview_new = """                var cameraHandler by remember { mutableStateOf<PlatformCameraHandler?>(null) }
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    flashEnabled = uiState.flashMode != FlashMode.OFF,
                    onEdgeDetected = { quad -> viewModel.onEdgeDetected(quad) },
                    onCameraBind = { handler -> cameraHandler = handler }
                )"""

content = content.replace(preview_old, preview_new)

shutter_old = """                ShutterButton(
                    isAutoCapture = uiState.isAutoCaptureOn,
                    autoCaptureProgress = uiState.autoCaptureProgress,
                    isBatchMode = uiState.isBatchMode,
                    batchCount = uiState.batchCount,
                    onClick = {
                        val capturedPath = "scan_page_${uiState.batchCount + 1}.jpg"
                        viewModel.onPhotoCaptured(capturedPath)
                        if (!uiState.isBatchMode) {
                            onNavigateToCrop()
                        }
                    }
                )"""

shutter_new = """                ShutterButton(
                    isAutoCapture = uiState.isAutoCaptureOn,
                    autoCaptureProgress = uiState.autoCaptureProgress,
                    isBatchMode = uiState.isBatchMode,
                    batchCount = uiState.batchCount,
                    onClick = {
                        cameraHandler?.capturePhoto { capturedPath ->
                            viewModel.onPhotoCaptured(capturedPath)
                            if (!uiState.isBatchMode) {
                                onNavigateToCrop()
                            }
                        }
                    }
                )"""

content = content.replace(shutter_old, shutter_new)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'w') as f:
    f.write(content)
