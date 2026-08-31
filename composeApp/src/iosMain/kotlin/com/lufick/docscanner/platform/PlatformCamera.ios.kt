package com.lufick.docscanner.platform

import com.lufick.docscanner.util.currentTimeMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lufick.docscanner.model.QuadCorners

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    flashEnabled: Boolean,
    zoomRatio: Float,
    isQrScanMode: Boolean,
    onEdgeDetected: (QuadCorners) -> Unit,
    onQrDetected: (payload: String, qrBoundingRatio: Float) -> Unit,
    onCameraBind: (PlatformCameraHandler) -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black))
    
    onCameraBind(object : PlatformCameraHandler {
        override fun capturePhoto(onPhotoCaptured: (imagePath: String) -> Unit) {
            onPhotoCaptured("ios_captured_scan_${currentTimeMillis()}.jpg")
        }
        override fun toggleFlash(enabled: Boolean) {}
        override fun setZoom(ratio: Float) {}
        override fun resetZoom() {}
    })
}
