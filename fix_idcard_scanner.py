import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/IdCardScannerScreen.kt', 'r') as f:
    content = f.read()

# Make sure to import CameraPreview, PlatformCameraHandler, LocalImage
if "com.lufick.docscanner.platform.CameraPreview" not in content:
    content = content.replace("import com.lufick.docscanner.viewmodel.IdCardViewModel", 
                              "import com.lufick.docscanner.viewmodel.IdCardViewModel\nimport com.lufick.docscanner.platform.CameraPreview\nimport com.lufick.docscanner.platform.PlatformCameraHandler\nimport com.lufick.docscanner.platform.LocalImage\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue")

old_viewfinder = """                // ID Card Viewfinder Frame
                Box(
                    modifier = Modifier
                        .size(320.dp, 200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.DarkGray.copy(alpha = 0.4f))
                        .border(2.dp, LufickEmerald, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.currentSide == IdCardSide.FRONT) "FRONT OF CARD" else "BACK OF CARD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(LufickEmerald.copy(alpha = 0.3f))
                        .clickable {
                            if (uiState.currentSide == IdCardSide.FRONT) {
                                viewModel.onFrontCaptured("front_id.jpg")
                            } else {
                                viewModel.onBackCaptured("back_id.jpg")
                            }
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }"""

new_viewfinder = """                var cameraHandler by remember { mutableStateOf<PlatformCameraHandler?>(null) }
                
                Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onCameraBind = { handler -> cameraHandler = handler }
                    )
                    
                    // ID Card Guide Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(320.dp, 200.dp)
                            .border(2.dp, LufickEmerald, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(8.dp))) {
                            Text(
                                text = if (uiState.currentSide == IdCardSide.FRONT) "FRONT OF CARD" else "BACK OF CARD",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(LufickEmerald.copy(alpha = 0.3f))
                        .clickable {
                            cameraHandler?.capturePhoto { capturedPath ->
                                if (uiState.currentSide == IdCardSide.FRONT) {
                                    viewModel.onFrontCaptured(capturedPath)
                                } else {
                                    viewModel.onBackCaptured(capturedPath)
                                }
                            }
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }"""

content = content.replace(old_viewfinder, new_viewfinder)

old_stitched = """                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Front ID Frame
                        Box(
                            modifier = Modifier
                                .size(240.dp, 140.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("FRONT SIDE SCANNED", fontSize = 10.sp, color = Color.DarkGray)
                        }

                        // Back ID Frame
                        Box(
                            modifier = Modifier
                                .size(240.dp, 140.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BACK SIDE SCANNED", fontSize = 10.sp, color = Color.DarkGray)
                        }
                    }"""

new_stitched = """                    // Render stitched image if available
                    if (uiState.stitchedImagePath != null) {
                        LocalImage(
                            path = uiState.stitchedImagePath!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Stitching in progress
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Processing...", color = Color.Gray)
                        }
                    }"""

content = content.replace(old_stitched, new_stitched)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/IdCardScannerScreen.kt', 'w') as f:
    f.write(content)
