import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'r') as f:
    content = f.read()

# Make sure mutableStateOf, remember, etc are imported
if "androidx.compose.runtime.mutableStateOf" not in content:
    content = content.replace("import androidx.compose.runtime.getValue", "import androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue")

old_preview = """        // 1. Live Camera Preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            flashEnabled = uiState.flashMode == FlashMode.ON || uiState.flashMode == FlashMode.TORCH,
            onEdgeDetected = { viewModel.onEdgeDetected(it) }
        )"""

new_preview = """        var cameraHandler by remember { mutableStateOf<PlatformCameraHandler?>(null) }
        
        // 1. Live Camera Preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            flashEnabled = uiState.flashMode == FlashMode.ON || uiState.flashMode == FlashMode.TORCH,
            onEdgeDetected = { viewModel.onEdgeDetected(it) },
            onCameraBind = { handler -> cameraHandler = handler }
        )"""

content = content.replace(old_preview, new_preview)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'w') as f:
    f.write(content)
