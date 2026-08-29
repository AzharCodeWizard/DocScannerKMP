package com.lufick.docscanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.theme.BoundingBoxColor
import com.lufick.docscanner.theme.CornerPinColor
import com.lufick.docscanner.theme.LufickEmerald
import kotlin.math.hypot

@Composable
fun QuadCropCanvas(
    modifier: Modifier = Modifier,
    corners: QuadCorners,
    onCornerMoved: (cornerIndex: Int, newPos: PointF) -> Unit,
    onDragEnd: () -> Unit
) {
    var activeHandle by remember { mutableStateOf<Int?>(null) }
    var touchScreenPos by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.fillMaxSize()) {
        // Main Interactive Crop Layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(corners) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val pts = listOf(
                                corners.topLeft,
                                corners.topRight,
                                corners.bottomRight,
                                corners.bottomLeft
                            )
                            val touchRadius = 80f

                            var closestIdx: Int? = null
                            var minDist = Float.MAX_VALUE

                            pts.forEachIndexed { idx, p ->
                                val px = p.x * w
                                val py = p.y * h
                                val d = hypot(px - offset.x, py - offset.y)
                                if (d < touchRadius && d < minDist) {
                                    minDist = d
                                    closestIdx = idx
                                }
                            }
                            activeHandle = closestIdx
                            touchScreenPos = offset
                        },
                        onDrag = { change, _ ->
                            val handle = activeHandle ?: return@detectDragGestures
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val clampedX = (change.position.x / w).coerceIn(0f, 1f)
                            val clampedY = (change.position.y / h).coerceIn(0f, 1f)
                            touchScreenPos = change.position
                            onCornerMoved(handle, PointF(clampedX, clampedY))
                        },
                        onDragEnd = {
                            activeHandle = null
                            onDragEnd()
                        },
                        onDragCancel = {
                            activeHandle = null
                            onDragEnd()
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height

            val tl = Offset(corners.topLeft.x * w, corners.topLeft.y * h)
            val tr = Offset(corners.topRight.x * w, corners.topRight.y * h)
            val br = Offset(corners.bottomRight.x * w, corners.bottomRight.y * h)
            val bl = Offset(corners.bottomLeft.x * w, corners.bottomLeft.y * h)

            // Crop Quad Path
            val path = Path().apply {
                moveTo(tl.x, tl.y)
                lineTo(tr.x, tr.y)
                lineTo(br.x, br.y)
                lineTo(bl.x, bl.y)
                close()
            }

            // Fill Tint
            drawPath(path, color = LufickEmerald.copy(alpha = 0.18f))
            // Quad Border
            drawPath(path, color = BoundingBoxColor, style = Stroke(width = 3.dp.toPx()))

            // 3x3 Grid inside Quad for Perspective Alignment
            for (i in 1..2) {
                val f = i / 3f
                val topEdge = Offset(tl.x + (tr.x - tl.x) * f, tl.y + (tr.y - tl.y) * f)
                val bottomEdge = Offset(bl.x + (br.x - bl.x) * f, bl.y + (br.y - bl.y) * f)
                drawLine(
                    color = LufickEmerald.copy(alpha = 0.35f),
                    start = topEdge,
                    end = bottomEdge,
                    strokeWidth = 1.dp.toPx()
                )

                val leftEdge = Offset(tl.x + (bl.x - tl.x) * f, tl.y + (bl.y - tl.y) * f)
                val rightEdge = Offset(tr.x + (br.x - tr.x) * f, tr.y + (br.y - tr.y) * f)
                drawLine(
                    color = LufickEmerald.copy(alpha = 0.35f),
                    start = leftEdge,
                    end = rightEdge,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Corner Pins
            val cornerOffsets = listOf(tl, tr, br, bl)
            cornerOffsets.forEachIndexed { index, pos ->
                val isSelected = activeHandle == index
                val pinRadius: Float = if (isSelected) 18.dp.toPx() else 14.dp.toPx()

                // Outer Shadow
                drawCircle(color = Color.Black.copy(alpha = 0.45f), radius = pinRadius + 5f, center = pos)
                // Main Pin
                drawCircle(color = CornerPinColor, radius = pinRadius, center = pos)
                // Center White Dot
                drawCircle(color = Color.White, radius = pinRadius * 0.4f, center = pos)
            }

            // Midpoint Edge Handles
            val midTop = Offset((tl.x + tr.x) / 2f, (tl.y + tr.y) / 2f)
            val midRight = Offset((tr.x + br.x) / 2f, (tr.y + br.y) / 2f)
            val midBottom = Offset((bl.x + br.x) / 2f, (bl.y + br.y) / 2f)
            val midLeft = Offset((tl.x + bl.x) / 2f, (tl.y + bl.y) / 2f)

            listOf(midTop, midRight, midBottom, midLeft).forEach { mid ->
                drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = 8.dp.toPx(), center = mid)
                drawCircle(color = LufickEmerald, radius = 6.dp.toPx(), center = mid)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = mid)
            }
        }

        // Live Magnifier Loupe (Floating in opposite top corner while user drags)
        AnimatedVisibility(
            visible = activeHandle != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(if (touchScreenPos.x > 500f) Alignment.TopStart else Alignment.TopEnd).padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, LufickEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Magnified Crosshair
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    drawLine(
                        color = Color.Red.copy(alpha = 0.8f),
                        start = Offset(cx - 20.dp.toPx(), cy),
                        end = Offset(cx + 20.dp.toPx(), cy),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.Red.copy(alpha = 0.8f),
                        start = Offset(cx, cy - 20.dp.toPx()),
                        end = Offset(cx, cy + 20.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(color = Color.Red, radius = 3.dp.toPx(), center = Offset(cx, cy))
                }

                Text(
                    text = "2.5x ZOOM",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                )
            }
        }
    }
}
