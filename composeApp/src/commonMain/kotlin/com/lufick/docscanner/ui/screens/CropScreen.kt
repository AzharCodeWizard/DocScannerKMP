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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.lufick.docscanner.engine.RenderedDocumentView
import com.lufick.docscanner.theme.LufickEmerald
import com.lufick.docscanner.ui.components.LufickTopBar
import com.lufick.docscanner.ui.components.QuadCropCanvas
import com.lufick.docscanner.viewmodel.CropAspectRatio
import com.lufick.docscanner.viewmodel.CropViewModel

@Composable
fun CropScreen(
    viewModel: CropViewModel,
    onBack: () -> Unit,
    onNavigateToFilter: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LufickTopBar(
                title = "Adjust & Crop",
                onBackClick = onBack,
                actions = {
                    Button(
                        onClick = onNavigateToFilter,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LufickEmerald),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Next", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Document Image Container with Interactive Crop Quad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Real Rendered Document Underneath
                RenderedDocumentView(
                    modifier = Modifier.fillMaxSize(),
                    templateType = uiState.templateType,
                    rotationDegrees = uiState.rotationDegrees
                )

                // Interactive 4-point Quad with Loupe Magnifier
                QuadCropCanvas(
                    corners = uiState.corners,
                    onCornerMoved = { idx, pos -> viewModel.updateCorner(idx, pos) },
                    onDragEnd = { viewModel.finishCornerDrag() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Aspect Ratio Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CropAspectRatio.entries) { ratio ->
                    val isSel = uiState.selectedAspectRatio == ratio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) LufickEmerald else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (isSel) LufickEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .clickable { viewModel.setAspectRatio(ratio) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = ratio.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions Toolbar (Auto-Fit, Rotate, Full-Page)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { viewModel.rotate90() }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, tint = LufickEmerald, modifier = Modifier.size(20.dp))
                        Text("Rotate 90°", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                TextButton(onClick = { viewModel.autoDetect() }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LufickEmerald, modifier = Modifier.size(20.dp))
                        Text("Auto-Fit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LufickEmerald)
                    }
                }

                TextButton(onClick = { viewModel.fullPage() }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CropFree, contentDescription = null, tint = LufickEmerald, modifier = Modifier.size(20.dp))
                        Text("Full Page", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
