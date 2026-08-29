package com.lufick.docscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.theme.LufickEmerald
import com.lufick.docscanner.ui.components.LufickTopBar
import com.lufick.docscanner.viewmodel.IdCardSide
import com.lufick.docscanner.viewmodel.IdCardViewModel
import com.lufick.docscanner.platform.CameraPreview
import com.lufick.docscanner.platform.PlatformCameraHandler
import com.lufick.docscanner.platform.LocalImage
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun IdCardScannerScreen(
    viewModel: IdCardViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LufickTopBar(
                title = "ID Card / Passport Mode",
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instruction Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LufickEmerald.copy(alpha = 0.15f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (uiState.currentSide) {
                        IdCardSide.FRONT -> "Step 1: Align FRONT side of ID card inside the frame"
                        IdCardSide.BACK -> "Step 2: Flip over and align BACK side of ID card"
                        IdCardSide.PREVIEW -> "Step 3: Preview stitched single-page ID card"
                    },
                    color = LufickEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            if (uiState.currentSide != IdCardSide.PREVIEW) {
                var cameraHandler by remember { mutableStateOf<PlatformCameraHandler?>(null) }
                
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
                }
            } else {
                // Stitched A4 Preview (Front on top, Back on bottom)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    // Render stitched image if available
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
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.retake() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("Retake", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = onDone,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LufickEmerald)
                    ) {
                        Text("Save Card", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
