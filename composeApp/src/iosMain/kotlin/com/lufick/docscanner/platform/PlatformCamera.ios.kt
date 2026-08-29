package com.lufick.docscanner.platform

import com.lufick.docscanner.util.currentTimeMillis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    flashEnabled: Boolean,
    onEdgeDetected: (QuadCorners) -> Unit,
    onCameraBind: (PlatformCameraHandler) -> Unit
) {
    // Mock iOS camera for now
    Box(modifier = modifier.fillMaxSize().background(Color.Black))
    
    // Bind mock handler
    onCameraBind(object : PlatformCameraHandler {
        override fun capturePhoto(onPhotoCaptured: (imagePath: String) -> Unit) {
            onPhotoCaptured("ios_captured_scan_${currentTimeMillis()}.jpg")
        }
        override fun toggleFlash(enabled: Boolean) {}
    })
}
