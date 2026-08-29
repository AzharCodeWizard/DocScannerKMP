package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lufick.docscanner.model.QuadCorners

@Composable
expect fun CameraPreview(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean = false,
    onEdgeDetected: (QuadCorners) -> Unit = {},
    onCameraBind: (PlatformCameraHandler) -> Unit = {}
)

interface PlatformCameraHandler {
    fun capturePhoto(onPhotoCaptured: (imagePath: String) -> Unit)
    fun toggleFlash(enabled: Boolean)
}
