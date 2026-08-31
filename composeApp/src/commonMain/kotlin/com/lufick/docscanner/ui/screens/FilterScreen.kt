package com.lufick.docscanner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.engine.FilterEngine
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.platform.LocalImage
import com.lufick.docscanner.theme.LufickEmerald
import com.lufick.docscanner.ui.components.LufickTopBar
import com.lufick.docscanner.viewmodel.FilterTab
import com.lufick.docscanner.viewmodel.FilterViewModel
import kotlin.math.roundToInt

@Composable
fun FilterScreen(
    viewModel: FilterViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 1. Calculate live GPU ColorMatrix
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

    // 2. Interactive Pinch-to-Zoom and 2D Pan Viewport State
    var zoomScale by remember { mutableStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(1.0f, 4.0f)
        if (zoomScale > 1.0f) {
            val maxPan = (zoomScale - 1.0f) * 450f
            val newX = (panOffset.x + offsetChange.x).coerceIn(-maxPan, maxPan)
            val newY = (panOffset.y + offsetChange.y).coerceIn(-maxPan, maxPan)
            panOffset = Offset(newX, newY)
        } else {
            panOffset = Offset.Zero
        }
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

                    // Reset All Adjustments
                    IconButton(onClick = {
                        viewModel.resetAll()
                        zoomScale = 1.0f
                        panOffset = Offset.Zero
                    }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset All",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Top-right Save Checkmark
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(0.35f)
                    ) {
                        Text("Retake", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onDone,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LufickEmerald),
                        modifier = Modifier.weight(0.65f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Document", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // -------------------------------------------------------------
            // 1. Studio Interactive Document Viewport (with Zoom & Pan)
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0B0F19))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                // Reset zoom and pan on double-tap
                                zoomScale = 1.0f
                                panOffset = Offset.Zero
                            }
                        )
                    }
                    .transformable(state = transformableState),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imagePath.isNotBlank()) {
                    LocalImage(
                        path = uiState.imagePath,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            },
                        colorFilter = composeColorFilter,
                        rotationDegrees = uiState.rotationDegrees
                    )
                } else {
                    CircularProgressIndicator(color = LufickEmerald)
                }

                // Top-Left: Active Preset Badge with Filter Dot
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, LufickEmerald.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isComparisonMode) Color.Yellow else LufickEmerald)
                    )
                    Text(
                        text = if (uiState.isComparisonMode) "Original (Raw)" else uiState.selectedFilter.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Top-Right: Zoom Level Pill (if zoomed in)
                if (zoomScale > 1.05f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${((zoomScale * 10).roundToInt() / 10f)}x (Double-tap to reset)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.LightGray
                        )
                    }
                }

                // Bottom Center: Hold-to-Compare Floating Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.82f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    viewModel.setComparisonMode(true)
                                    tryAwaitRelease()
                                    viewModel.setComparisonMode(false)
                                }
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Compare",
                            tint = if (uiState.isComparisonMode) LufickEmerald else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (uiState.isComparisonMode) "Showing Original..." else "Hold to Compare (Original)",
                            fontSize = 11.sp,
                            color = if (uiState.isComparisonMode) LufickEmerald else Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // -------------------------------------------------------------
            // 2. Multi-Tab Studio Enhancement Console
            // -------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Segmented Tab Selector (Filters, Adjust, Tools)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterTab.entries.forEach { tab ->
                        val isSelected = uiState.activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (isSelected) LufickEmerald else Color.Transparent)
                                .clickable { viewModel.setActiveTab(tab) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (tab) {
                                    FilterTab.PRESETS -> "✨ Filters"
                                    FilterTab.ADJUST -> "🎛️ Adjust"
                                    FilterTab.TOOLS -> "🛠️ Tools"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Tab Content Switcher
                when (uiState.activeTab) {
                    FilterTab.PRESETS -> {
                        // Visual Preset Carousel with Rich Miniature Cards
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(FilterType.entries) { filter ->
                                val isSelected = uiState.selectedFilter == filter
                                VisualFilterCard(
                                    filter = filter,
                                    isSelected = isSelected,
                                    onClick = { viewModel.selectFilter(filter) }
                                )
                            }
                        }
                    }

                    FilterTab.ADJUST -> {
                        // Fine-Tuning Sliders with Live Value Badges
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            // 1. Brightness Slider
                            AdjustmentSliderRow(
                                title = "Brightness",
                                value = uiState.brightness,
                                valueRange = 0.6f..1.4f,
                                displayValue = "${(((uiState.brightness - 1.0f) * 100).roundToInt()).let { if (it > 0) "+$it%" else "$it%" }}",
                                icon = Icons.Default.Brightness6,
                                onValueChange = { viewModel.setBrightness(it) },
                                onReset = { viewModel.resetBrightness() }
                            )

                            // 2. Contrast Slider
                            AdjustmentSliderRow(
                                title = "Contrast",
                                value = uiState.contrast,
                                valueRange = 0.6f..2.0f,
                                displayValue = "${((uiState.contrast * 100).roundToInt() / 100f)}x",
                                icon = Icons.Default.Contrast,
                                onValueChange = { viewModel.setContrast(it) },
                                onReset = { viewModel.resetContrast() }
                            )

                            // 3. Saturation Slider
                            AdjustmentSliderRow(
                                title = "Saturation",
                                value = uiState.saturation,
                                valueRange = 0.0f..2.0f,
                                displayValue = "${((uiState.saturation * 100).roundToInt())}%",
                                icon = Icons.Default.FormatColorFill,
                                onValueChange = { viewModel.setSaturation(it) },
                                onReset = { viewModel.resetSaturation() }
                            )
                        }
                    }

                    FilterTab.TOOLS -> {
                        // Quick Action Tools Grid
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ToolActionButton(
                                icon = Icons.Default.AutoAwesome,
                                label = "Auto Fix",
                                tint = LufickEmerald,
                                onClick = { viewModel.autoEnhance() }
                            )

                            ToolActionButton(
                                icon = Icons.Default.RotateRight,
                                label = "Rotate 90°",
                                tint = MaterialTheme.colorScheme.primary,
                                onClick = { viewModel.rotate90() }
                            )

                            ToolActionButton(
                                icon = Icons.Default.RotateLeft,
                                label = "Rotate -90°",
                                tint = MaterialTheme.colorScheme.primary,
                                onClick = { viewModel.rotateCounterClockwise() }
                            )

                            ToolActionButton(
                                icon = Icons.Default.RestartAlt,
                                label = "Reset All",
                                tint = Color(0xFFEF4444),
                                onClick = {
                                    viewModel.resetAll()
                                    zoomScale = 1.0f
                                    panOffset = Offset.Zero
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-End Visual Filter Preset Card with Styled Miniature Preview
 */
@Composable
private fun VisualFilterCard(
    filter: FilterType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(86.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) LufickEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Miniature Document Preview Thumbnail
            Box(
                modifier = Modifier
                    .size(46.dp, 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getFilterPreviewBrush(filter))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Miniature Document Text Lines
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(modifier = Modifier.size(24.dp, 3.dp).background(getFilterLineColor(filter)))
                    Box(modifier = Modifier.size(32.dp, 3.dp).background(getFilterLineColor(filter)))
                    Box(modifier = Modifier.size(20.dp, 3.dp).background(getFilterLineColor(filter)))
                    Box(modifier = Modifier.size(28.dp, 3.dp).background(getFilterLineColor(filter)))
                }

                // Selected Checkmark Badge
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(LufickEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            // Filter Name
            Text(
                text = filter.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) LufickEmerald else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Dynamic Preview Brush for Visual Filter Cards
 */
private fun getFilterPreviewBrush(filter: FilterType): Brush {
    return when (filter) {
        FilterType.ORIGINAL -> Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
        FilterType.MAGIC_COLOR_1 -> Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFD1FAE5), Color(0xFF6EE7B7)))
        FilterType.MAGIC_COLOR_2 -> Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE0F2FE), Color(0xFFBAE6FD)))
        FilterType.SUPER_CLEAN -> Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE0E7FF), Color(0xFFC7D2FE)))
        FilterType.SHARP_BW -> Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)))
        FilterType.GRAYSCALE -> Brush.verticalGradient(listOf(Color(0xFF94A3B8), Color(0xFF64748B)))
        FilterType.VIVID_PHOTO -> Brush.linearGradient(listOf(Color(0xFFFDE68A), Color(0xFFF472B6), Color(0xFF60A5FA)))
        FilterType.ECO_PRINT -> Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFDCFCE7)))
    }
}

/**
 * Dynamic Line Color for Miniature Previews
 */
private fun getFilterLineColor(filter: FilterType): Color {
    return when (filter) {
        FilterType.SHARP_BW, FilterType.SUPER_CLEAN -> Color.Black
        FilterType.MAGIC_COLOR_1 -> Color(0xFF065F46)
        FilterType.MAGIC_COLOR_2 -> Color(0xFF0369A1)
        FilterType.GRAYSCALE -> Color(0xFF334155)
        FilterType.VIVID_PHOTO -> Color(0xFF9D174D)
        FilterType.ECO_PRINT -> Color(0xFF15803D)
        FilterType.ORIGINAL -> Color(0xFF475569)
    }
}

/**
 * Adjustment Slider Row with Icon, Value Pill, and Reset Action
 */
@Composable
private fun AdjustmentSliderRow(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(72.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = LufickEmerald,
                activeTrackColor = LufickEmerald,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            modifier = Modifier.weight(1f)
        )

        // Live Value Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .widthIn(min = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayValue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LufickEmerald
            )
        }

        // Quick Reset Icon
        IconButton(
            onClick = onReset,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Quick Tool Action Button
 */
@Composable
private fun ToolActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.16f))
                .border(1.dp, tint.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
