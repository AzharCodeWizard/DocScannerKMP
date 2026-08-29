import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'r') as f:
    content = f.read()

# Add auto-capture trigger LaunchedEffect
if "LaunchedEffect(uiState.autoCaptureProgress)" not in content:
    auto_trigger_code = """
    // Auto-Capture Shutter Trigger
    LaunchedEffect(uiState.autoCaptureProgress) {
        if (uiState.autoCaptureProgress >= 1.0f && uiState.isAutoCaptureOn) {
            cameraHandler?.capturePhoto { capturedPath ->
                viewModel.onPhotoCaptured(capturedPath)
                if (!uiState.isBatchMode) {
                    onNavigateToCrop()
                }
            }
        }
    }
"""
    content = content.replace("val uiState by viewModel.uiState.collectAsState()", "val uiState by viewModel.uiState.collectAsState()\n" + auto_trigger_code)

# Remove the colliding floating status and zoom bar (section 6)
old_sec_6 = """        // 6. Detection Status & Zoom Bar (Mid-Bottom Floating)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 190.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Detection Status Banner
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.70f))
                    .border(1.dp, LufickEmerald.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LufickEmerald)
                )
                Text(
                    text = uiState.detectionState.message,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Zoom Level Selector (0.5x, 1x, 2x)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0.5f, 1.0f, 2.0f).forEach { zoom ->
                    val isSel = uiState.zoomRatio == zoom
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) LufickEmerald else Color.Transparent)
                            .clickable { viewModel.setZoom(zoom) }
                            .padding(horizontal = 10.dp, vertical = 3.dp)
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
        }"""

content = content.replace(old_sec_6, "")

# Now replace section 7 bottom controls panel
old_sec_7 = """        // 7. Bottom Controls Panel (Thumbnails, Modes & Big Shutter)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.82f))
                .padding(bottom = 28.dp, top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) LufickEmerald.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewModel.setScanMode(mode) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = mode.title.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) LufickEmerald else Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Shutter Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Import Button
                IconButton(
                    onClick = onNavigateToCrop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.DarkGray.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
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

                // Finish / Next Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (uiState.batchCount > 0) LufickEmerald else Color.DarkGray.copy(alpha = 0.6f))
                        .border(1.dp, if (uiState.batchCount > 0) LufickEmerald else Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToCrop() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (uiState.batchCount > 0) "DONE" else "NEXT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.batchCount > 0) Color.Black else Color.White
                        )
                        if (uiState.batchCount > 0) {
                            Text(
                                text = "(${uiState.batchCount})",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }"""

new_sec_7 = """        // 6. Modern Bottom Controls Panel
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
        }"""

content = content.replace(old_sec_7, new_sec_7)

# Make sure AutoMirrored icon is imported
if "androidx.compose.material.icons.automirrored.filled.ArrowForward" not in content:
    content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.filled.ArrowForward")

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'w') as f:
    f.write(content)
