package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lufick.docscanner.model.QuadCorners

@Composable
expect fun CameraPreview(
    modifier: Modifier = Modifier,
    flashEnabled: Boolean = false,
    isQrScanMode: Boolean = false,
    onEdgeDetected: (QuadCorners) -> Unit = {},
    onQrDetected: (payload: String, qrBoundingRatio: Float) -> Unit = { _, _ -> },
    onCameraBind: (PlatformCameraHandler) -> Unit = {}
)

interface PlatformCameraHandler {
    fun capturePhoto(onPhotoCaptured: (imagePath: String) -> Unit)
    fun toggleFlash(enabled: Boolean)
    fun setZoom(ratio: Float)
    fun resetZoom()
}
