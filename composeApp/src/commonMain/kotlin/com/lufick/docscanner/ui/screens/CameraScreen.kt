package com.lufick.docscanner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.model.ScanMode
import com.lufick.docscanner.platform.CameraPreview
import com.lufick.docscanner.platform.PlatformCameraHandler
import com.lufick.docscanner.theme.LufickEmerald
import com.lufick.docscanner.ui.components.BatchThumbnailReel
import com.lufick.docscanner.ui.components.BookGuideOverlay
import com.lufick.docscanner.ui.components.DocumentQuadOverlay
import com.lufick.docscanner.ui.components.IdCardGuideOverlay
import com.lufick.docscanner.ui.components.PassportGuideOverlay
import com.lufick.docscanner.ui.components.QrScannerOverlay
import com.lufick.docscanner.ui.components.RuleOfThirdsGrid
import com.lufick.docscanner.ui.components.ShutterButton
import com.lufick.docscanner.ui.components.TapToFocusRing
import com.lufick.docscanner.viewmodel.CameraViewModel
import com.lufick.docscanner.viewmodel.FlashMode

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onClose: () -> Unit,
    onNavigateToCrop: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var cameraHandler by remember { mutableStateOf<PlatformCameraHandler?>(null) }

    var isCapturing by remember { mutableStateOf(false) }

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
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    viewModel.onScreenTapped(offset)
                }
            }
    ) {
        
        
        // 1. Live Camera Preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            flashEnabled = uiState.flashMode == FlashMode.ON || uiState.flashMode == FlashMode.TORCH,
            isQrScanMode = (uiState.scanMode == ScanMode.QR_CODE),
            onEdgeDetected = { viewModel.onEdgeDetected(it) },
            onCameraBind = { handler -> cameraHandler = handler }
        )

        // 2. Mode-Specific Overlays
        when (uiState.scanMode) {
            ScanMode.DOCUMENT -> {
                DocumentQuadOverlay(
                    quad = uiState.detectedQuad,
                    isDetected = true,
                    showLaser = true
                )
            }
            ScanMode.ID_CARD -> {
                IdCardGuideOverlay(isFrontSide = uiState.isIdCardFront)
            }
            ScanMode.BOOK -> {
                BookGuideOverlay()
            }
            ScanMode.PASSPORT -> {
                PassportGuideOverlay()
            }
            ScanMode.QR_CODE -> {
                QrScannerOverlay()
            }
        }

        // 3. Rule of Thirds Grid (Optional)
        if (uiState.isGridVisible) {
            RuleOfThirdsGrid()
        }

        // 4. Tap-to-Focus Animation Ring
        TapToFocusRing(point = uiState.tapFocusPoint)

        // 5. Top Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 42.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close Button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            // Central Status & Auto/Batch Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto/Manual Toggle
                Text(
                    text = if (uiState.isAutoCaptureOn) "AUTO" else "MANUAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isAutoCaptureOn) LufickEmerald else Color.LightGray,
                    modifier = Modifier.clickable { viewModel.toggleAutoCapture() }
                )

                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(width = 1.dp, height = 12.dp).background(Color.Gray.copy(alpha = 0.4f)))
                Spacer(modifier = Modifier.width(10.dp))

                // Batch Mode Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleBatchMode() }
                ) {
                    Text(
                        text = "BATCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isBatchMode) LufickEmerald else Color.LightGray
                    )
                    if (uiState.batchCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(LufickEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.batchCount}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Right Quick Controls (Flash & Grid)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Grid Toggle
                IconButton(
                    onClick = { viewModel.toggleGrid() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isGridVisible) LufickEmerald.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, if (uiState.isGridVisible) LufickEmerald else Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.GridOn,
                        contentDescription = "Grid",
                        tint = if (uiState.isGridVisible) LufickEmerald else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Flash Cycle Button
                IconButton(
                    onClick = { viewModel.cycleFlashMode() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (uiState.flashMode != FlashMode.OFF) LufickEmerald.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, if (uiState.flashMode != FlashMode.OFF) LufickEmerald else Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = when (uiState.flashMode) {
                            FlashMode.OFF -> Icons.Default.FlashOff
                            FlashMode.ON -> Icons.Default.FlashOn
                            FlashMode.AUTO -> Icons.Default.FlashAuto
                            FlashMode.TORCH -> Icons.Default.Highlight
                        },
                        contentDescription = "Flash Mode",
                        tint = if (uiState.flashMode != FlashMode.OFF) Color.Yellow else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }



        // 6. Modern Bottom Controls Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.95f),
                            Color.Black
                        )
                    )
                )
                .padding(bottom = 24.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Status & Zoom Row (Cleanly placed at top of bottom controls)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Detection Status Banner
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, LufickEmerald.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (uiState.detectionState == com.lufick.docscanner.viewmodel.DetectionState.HOLD_STILL) Color(0xFF10B981) else LufickEmerald)
                    )
                    Text(
                        text = uiState.detectionState.message,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Zoom Selector (.5x, 1x, 2x)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0.5f, 1.0f, 2.0f).forEach { zoom ->
                        val isSel = uiState.zoomRatio == zoom
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) LufickEmerald else Color.Transparent)
                                .clickable { viewModel.setZoom(zoom) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${if (zoom == 0.5f) ".5" else zoom.toInt()}x",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            // Batch Thumbnail Reel (if pages captured)
            if (uiState.capturedImages.isNotEmpty()) {
                BatchThumbnailReel(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thumbnails = uiState.capturedImages,
                    onRemoveThumbnail = { viewModel.removeCapturedPage(it) },
                    onThumbnailClick = { onNavigateToCrop() }
                )
            }

            // Mode Selector Pill Carousel
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(ScanMode.entries) { mode ->
                    val isSelected = uiState.scanMode == mode
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) LufickEmerald.copy(alpha = 0.22f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) LufickEmerald.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setScanMode(mode) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = mode.title.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) LufickEmerald else Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Shutter Button Row with Redesigned Action / Done Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Import Button
                IconButton(
                    onClick = onNavigateToCrop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1E222D))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = "Import Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Main High-Precision Shutter Button
                ShutterButton(
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
                )

                // Redesigned Modern Action / DONE Button
                val hasBatchPages = uiState.batchCount > 0
                Box(
                    modifier = Modifier
                        .height(54.dp)
                        .then(
                            if (hasBatchPages) Modifier.widthIn(min = 96.dp)
                            else Modifier.size(54.dp)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (hasBatchPages) androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(LufickEmerald, Color(0xFF10B981))
                            )
                            else androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(Color(0xFF262A35), Color(0xFF1E222D))
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (hasBatchPages) Color(0xFF6EE7B7) else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onNavigateToCrop() }
                        .padding(horizontal = if (hasBatchPages) 12.dp else 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasBatchPages) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${uiState.batchCount}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                            Text(
                                text = "DONE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                letterSpacing = 0.5.sp
                            )
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Done",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
