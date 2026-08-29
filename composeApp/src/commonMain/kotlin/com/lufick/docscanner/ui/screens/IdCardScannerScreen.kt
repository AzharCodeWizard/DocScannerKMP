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
                // ID Card Viewfinder Frame
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
                    Column(
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
