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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.platform.CameraPreview
import com.lufick.docscanner.platform.LocalImage
import com.lufick.docscanner.platform.PlatformCameraHandler
import com.lufick.docscanner.platform.rememberPlatformPdfEngine
import com.lufick.docscanner.platform.rememberPlatformShare
import com.lufick.docscanner.theme.LufickEmerald
import com.lufick.docscanner.ui.components.LufickTopBar
import com.lufick.docscanner.viewmodel.IdCardSide
import com.lufick.docscanner.viewmodel.IdCardViewModel
import kotlinx.coroutines.launch

@Composable
fun IdCardScannerScreen(
    viewModel: IdCardViewModel,
    onBack: () -> Unit,
    onDone: (docId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val pdfEngine = rememberPlatformPdfEngine()
    val platformShare = rememberPlatformShare()

    var showSaveDialog by remember { mutableStateOf(false) }
    var cardTitle by remember { mutableStateOf("National ID Card") }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save ID Card Document", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a document title for this 2-in-1 ID card scan:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cardTitle,
                        onValueChange = { cardTitle = it },
                        label = { Text("ID Card Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        viewModel.saveCard(cardTitle) { docId ->
                            onDone(docId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LufickEmerald)
                ) {
                    Text("Save to Vault", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            LufickTopBar(
                title = "2-in-1 ID Card Scanner",
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
                        IdCardSide.PREVIEW -> "Step 3: Stitched 2-in-1 A4 Canvas Ready"
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
                        Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.65f)).padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(8.dp))) {
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
                        .background(Color(0xFF0F172A))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    if (uiState.stitchedImagePath != null && !uiState.isStitching) {
                        LocalImage(
                            path = uiState.stitchedImagePath!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = LufickEmerald)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Stitching Front & Back onto A4 Canvas...", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
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
                        Text("Retake", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val stitched = uiState.stitchedImagePath ?: return@Button
                            scope.launch {
                                val pdfPath = pdfEngine.createPdf(
                                    imagePaths = listOf(stitched),
                                    title = "ID_Card_${cardTitle.replace(" ", "_")}",
                                    config = com.lufick.docscanner.model.PdfConfig()
                                )
                                platformShare.shareFile(pdfPath, "application/pdf")
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LufickEmerald)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Card", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
