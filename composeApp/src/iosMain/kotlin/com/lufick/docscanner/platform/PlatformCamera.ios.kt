package com.lufick.docscanner.platform

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
    onEdgeDetected: (QuadCorners) -> Unit
) {
    // iOS AVFoundation camera view or simulated canvas preview
    Box(modifier = modifier.fillMaxSize().background(Color.Black))
}

actual class PlatformCameraHandler {
    actual fun capturePhoto(onPhotoCaptured: (imagePath: String) -> Unit) {
        onPhotoCaptured("ios_captured_scan_${System.currentTimeMillis()}.jpg")
    }

    actual fun toggleFlash(enabled: Boolean) {
        // AVFoundation AVCaptureDevice torch mode
    }
}
