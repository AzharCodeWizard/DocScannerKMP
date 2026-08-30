package com.lufick.docscanner.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.engine.FilterEngine
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.platform.LocalImage
import com.lufick.docscanner.theme.LufickEmerald
import com.lufick.docscanner.ui.components.LufickTopBar
import com.lufick.docscanner.viewmodel.FilterTab
import com.lufick.docscanner.viewmodel.FilterViewModel

@Composable
fun FilterScreen(
    viewModel: FilterViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Calculate live GPU ColorMatrix for the active filter and slider settings
    val colorMatrixValues = remember(
        uiState.selectedFilter,
        uiState.brightness,
        uiState.contrast,
        uiState.saturation
    ) {
        FilterEngine.getColorMatrixForFilter(
            filter = uiState.selectedFilter,
            brightness = uiState.brightness,
            contrast = uiState.contrast,
            saturation = uiState.saturation
        ).values
    }

    val composeColorFilter = if (uiState.isComparisonMode) {
        null
    } else {
        ColorFilter.colorMatrix(ColorMatrix(colorMatrixValues))
    }

    Scaffold(
        topBar = {
            LufickTopBar(
                title = "Color & Enhancement",
                onBackClick = onBack,
                actions = {
                    // Quick Auto-Enhance Button
                    IconButton(onClick = { viewModel.autoEnhance() }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Auto Enhance",
                            tint = LufickEmerald
                        )
                    }

                    // Rotate 90° Button
                    IconButton(onClick = { viewModel.rotate90() }) {
                        Icon(
                            imageVector = Icons.Default.CropRotate,
                            contentDescription = "Rotate",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Quick Top Save Icon
                    IconButton(onClick = onDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = LufickEmerald
                        )
                    }
                }
            )
        },
        bottomBar = {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.35f)
                    ) {
                        Text("Retake", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onDone,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LufickEmerald),
                        modifier = Modifier.weight(0.65f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Document", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Live Enhanced Document Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imagePath.isNotBlank()) {
                    // Real Camera/Gallery Image with live GPU ColorMatrix
                    LocalImage(
                        path = uiState.imagePath,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        colorFilter = composeColorFilter,
                        rotationDegrees = uiState.rotationDegrees
                    )
                } else {
                    CircularProgressIndicator(color = LufickEmerald)
                }

                // Active Preset Status Pill in Top-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(1.dp, LufickEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (uiState.isComparisonMode) "Original (Raw)" else uiState.selectedFilter.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isComparisonMode) Color.Yellow else LufickEmerald
                    )
                }

                // Hold-to-Compare Floating Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    viewModel.setComparisonMode(true)
                                    tryAwaitRelease()
                                    viewModel.setComparisonMode(false)
                                }
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Compare",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Hold to compare (Original)",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Multi-Mode Enhancement Console
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tab Header: Presets vs Fine-Tune Sliders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Presets Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (uiState.activeTab == FilterTab.PRESETS) LufickEmerald else Color.Transparent)
                            .clickable { viewModel.setActiveTab(FilterTab.PRESETS) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨ Filter Presets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.activeTab == FilterTab.PRESETS) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Adjust Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (uiState.activeTab == FilterTab.ADJUST) LufickEmerald else Color.Transparent)
                            .clickable { viewModel.setActiveTab(FilterTab.ADJUST) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎛️ Adjust & Sliders",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.activeTab == FilterTab.ADJUST) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Content based on Active Tab
                when (uiState.activeTab) {
                    FilterTab.PRESETS -> {
                        // Horizontal Preset Carousel
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(FilterType.entries) { filter ->
                                val isSelected = uiState.selectedFilter == filter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) LufickEmerald.copy(alpha = 0.22f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                        .border(
                                            2.dp,
                                            if (isSelected) LufickEmerald else Color.Transparent,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.selectFilter(filter) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = filter.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) LufickEmerald else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = filter.description.take(18) + "...",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    FilterTab.ADJUST -> {
                        // Detailed Sliders: Contrast, Brightness, Saturation
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1. Contrast Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Contrast Boost", style = MaterialTheme.typography.bodySmall)
                                Text("${(uiState.contrast * 100).toInt()}%", color = LufickEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = uiState.contrast,
                                onValueChange = { viewModel.setContrast(it) },
                                valueRange = 0.6f..2.2f,
                                colors = SliderDefaults.colors(thumbColor = LufickEmerald, activeTrackColor = LufickEmerald)
                            )

                            // 2. Brightness Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Brightness / White Lift", style = MaterialTheme.typography.bodySmall)
                                val bVal = ((uiState.brightness - 1.0f) * 100).toInt()
                                Text("${if (bVal > 0) "+$bVal" else "$bVal"}%", color = LufickEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = uiState.brightness,
                                onValueChange = { viewModel.setBrightness(it) },
                                valueRange = 0.7f..1.3f,
                                colors = SliderDefaults.colors(thumbColor = LufickEmerald, activeTrackColor = LufickEmerald)
                            )

                            // 3. Saturation Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Color Saturation", style = MaterialTheme.typography.bodySmall)
                                Text("${(uiState.saturation * 100).toInt()}%", color = LufickEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = uiState.saturation,
                                onValueChange = { viewModel.setSaturation(it) },
                                valueRange = 0.0f..2.0f,
                                colors = SliderDefaults.colors(thumbColor = LufickEmerald, activeTrackColor = LufickEmerald)
                            )

                            // Reset Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.resetAdjustments() },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reset Sliders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

