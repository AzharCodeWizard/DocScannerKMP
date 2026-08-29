package com.lufick.docscanner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
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
    onCornersChanged: (QuadCorners) -> Unit,
    onDragEnd: () -> Unit = {}
) {
    var localCorners by remember { mutableStateOf(corners) }
    
    // Sync external changes (e.g. aspect ratio chips, auto-detect, rotate)
    androidx.compose.runtime.LaunchedEffect(corners) {
        localCorners = corners
    }

    val onCornersChangedState by rememberUpdatedState(onCornersChanged)
    val onDragEndState by rememberUpdatedState(onDragEnd)

    var activeHandle by remember { mutableStateOf<Int?>(null) }
    var touchPos by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        if (w <= 0f || h <= 0f) return@awaitEachGesture

                        var c = localCorners
                        val tl = Offset(c.topLeft.x * w, c.topLeft.y * h)
                        val tr = Offset(c.topRight.x * w, c.topRight.y * h)
                        val br = Offset(c.bottomRight.x * w, c.bottomRight.y * h)
                        val bl = Offset(c.bottomLeft.x * w, c.bottomLeft.y * h)

                        val midT = Offset((tl.x + tr.x) / 2f, (tl.y + tr.y) / 2f)
                        val midR = Offset((tr.x + br.x) / 2f, (tr.y + br.y) / 2f)
                        val midB = Offset((bl.x + br.x) / 2f, (bl.y + br.y) / 2f)
                        val midL = Offset((tl.x + bl.x) / 2f, (tl.y + bl.y) / 2f)

                        val cornerTouchRadius = 180f
                        val edgeTouchRadius = 140f

                        // 1. Check corner pins (0: TL, 1: TR, 2: BR, 3: BL)
                        val cornerPts = listOf(tl, tr, br, bl)
                        var matchedHandle: Int? = null
                        var minDist = Float.MAX_VALUE

                        cornerPts.forEachIndexed { index, pt ->
                            val d = hypot(pt.x - down.position.x, pt.y - down.position.y)
                            if (d < cornerTouchRadius && d < minDist) {
                                minDist = d
                                matchedHandle = index
                            }
                        }

                        // 2. Check edge midpoints (4: Top, 5: Right, 6: Bottom, 7: Left)
                        if (matchedHandle == null) {
                            val edgePts = listOf(midT, midR, midB, midL)
                            edgePts.forEachIndexed { index, pt ->
                                val d = hypot(pt.x - down.position.x, pt.y - down.position.y)
                                if (d < edgeTouchRadius && d < minDist) {
                                    minDist = d
                                    matchedHandle = index + 4
                                }
                            }
                        }

                        // 3. Check inside quad (8: Whole quad pan)
                        if (matchedHandle == null) {
                            val minX = minOf(tl.x, tr.x, br.x, bl.x)
                            val maxX = maxOf(tl.x, tr.x, br.x, bl.x)
                            val minY = minOf(tl.y, tr.y, br.y, bl.y)
                            val maxY = maxOf(tl.y, tr.y, br.y, bl.y)
                            if (down.position.x in minX..maxX && down.position.y in minY..maxY) {
                                matchedHandle = 8
                            }
                        }

                        if (matchedHandle == null) return@awaitEachGesture

                        activeHandle = matchedHandle
                        touchPos = down.position
                        down.consume()

                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) {
                                break
                            }

                            val curX = (change.position.x / w).coerceIn(0f, 1f)
                            val curY = (change.position.y / h).coerceIn(0f, 1f)
                            val delta = change.positionChange()
                            val dx = delta.x / w
                            val dy = delta.y / h

                            c = localCorners
                            val updated = when (matchedHandle) {
                                0 -> c.copy(topLeft = PointF(curX, curY))
                                1 -> c.copy(topRight = PointF(curX, curY))
                                2 -> c.copy(bottomRight = PointF(curX, curY))
                                3 -> c.copy(bottomLeft = PointF(curX, curY))
                                4 -> c.copy( // Move Top Edge
                                    topLeft = PointF(c.topLeft.x, curY.coerceAtMost(c.bottomLeft.y - 0.05f)),
                                    topRight = PointF(c.topRight.x, curY.coerceAtMost(c.bottomRight.y - 0.05f))
                                )
                                5 -> c.copy( // Move Right Edge
                                    topRight = PointF(curX.coerceAtLeast(c.topLeft.x + 0.05f), c.topRight.y),
                                    bottomRight = PointF(curX.coerceAtLeast(c.bottomLeft.x + 0.05f), c.bottomRight.y)
                                )
                                6 -> c.copy( // Move Bottom Edge
                                    bottomLeft = PointF(c.bottomLeft.x, curY.coerceAtLeast(c.topLeft.y + 0.05f)),
                                    bottomRight = PointF(c.bottomRight.x, curY.coerceAtLeast(c.topRight.y + 0.05f))
                                )
                                7 -> c.copy( // Move Left Edge
                                    topLeft = PointF(curX.coerceAtMost(c.topRight.x - 0.05f), c.topLeft.y),
                                    bottomLeft = PointF(curX.coerceAtMost(c.bottomRight.x - 0.05f), c.bottomLeft.y)
                                )
                                8 -> { // Pan entire quad
                                    val newTL = PointF((c.topLeft.x + dx).coerceIn(0f, 1f), (c.topLeft.y + dy).coerceIn(0f, 1f))
                                    val newTR = PointF((c.topRight.x + dx).coerceIn(0f, 1f), (c.topRight.y + dy).coerceIn(0f, 1f))
                                    val newBR = PointF((c.bottomRight.x + dx).coerceIn(0f, 1f), (c.bottomRight.y + dy).coerceIn(0f, 1f))
                                    val newBL = PointF((c.bottomLeft.x + dx).coerceIn(0f, 1f), (c.bottomLeft.y + dy).coerceIn(0f, 1f))
                                    c.copy(topLeft = newTL, topRight = newTR, bottomRight = newBR, bottomLeft = newBL)
                                }
                                else -> c
                            }

                            localCorners = updated
                            touchPos = change.position
                            change.consume()
                            onCornersChangedState(updated)
                        }

                        activeHandle = null
                        onDragEndState()
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            val cornersToDraw = localCorners
            val tl = Offset(cornersToDraw.topLeft.x * w, cornersToDraw.topLeft.y * h)
            val tr = Offset(cornersToDraw.topRight.x * w, cornersToDraw.topRight.y * h)
            val br = Offset(cornersToDraw.bottomRight.x * w, cornersToDraw.bottomRight.y * h)
            val bl = Offset(cornersToDraw.bottomLeft.x * w, cornersToDraw.bottomLeft.y * h)

            // Outer Dimming Layer
            drawRect(color = Color.Black.copy(alpha = 0.45f))

            // Highlight Quad
            val path = Path().apply {
                moveTo(tl.x, tl.y)
                lineTo(tr.x, tr.y)
                lineTo(br.x, br.y)
                lineTo(bl.x, bl.y)
                close()
            }

            drawPath(path, color = LufickEmerald.copy(alpha = 0.14f))
            drawPath(path, color = BoundingBoxColor, style = Stroke(width = 2.5.dp.toPx()))

            // 3x3 Grid
            for (i in 1..2) {
                val f = i / 3f
                val topEdge = Offset(tl.x + (tr.x - tl.x) * f, tl.y + (tr.y - tl.y) * f)
                val bottomEdge = Offset(bl.x + (br.x - bl.x) * f, bl.y + (br.y - bl.y) * f)
                drawLine(
                    color = Color.White.copy(alpha = 0.45f),
                    start = topEdge,
                    end = bottomEdge,
                    strokeWidth = 1.dp.toPx()
                )

                val leftEdge = Offset(tl.x + (bl.x - tl.x) * f, tl.y + (bl.y - tl.y) * f)
                val rightEdge = Offset(tr.x + (br.x - tr.x) * f, tr.y + (br.y - tr.y) * f)
                drawLine(
                    color = Color.White.copy(alpha = 0.45f),
                    start = leftEdge,
                    end = rightEdge,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Edge Midpoint Handles (Top, Right, Bottom, Left)
            val midT = Offset((tl.x + tr.x) / 2f, (tl.y + tr.y) / 2f)
            val midR = Offset((tr.x + br.x) / 2f, (tr.y + br.y) / 2f)
            val midB = Offset((bl.x + br.x) / 2f, (bl.y + br.y) / 2f)
            val midL = Offset((tl.x + bl.x) / 2f, (tl.y + bl.y) / 2f)

            listOf(midT, midR, midB, midL).forEachIndexed { idx, mid ->
                val isEdgeActive = activeHandle == (idx + 4)
                val radius = if (isEdgeActive) 10.dp.toPx() else 7.dp.toPx()
                drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = radius + 3f, center = mid)
                drawCircle(color = if (isEdgeActive) Color.White else LufickEmerald, radius = radius, center = mid)
                drawCircle(color = CornerPinColor, radius = radius * 0.5f, center = mid)
            }

            // 4 High-Precision Corner Pins
            listOf(tl, tr, br, bl).forEachIndexed { index, pos ->
                val isSelected = activeHandle == index
                val pinRadius = if (isSelected) 20.dp.toPx() else 14.dp.toPx()

                drawCircle(color = Color.Black.copy(alpha = 0.5f), radius = pinRadius + 4f, center = pos)
                drawCircle(color = Color.White, radius = pinRadius, center = pos)
                drawCircle(color = if (isSelected) Color(0xFF06B6D4) else CornerPinColor, radius = pinRadius - 3.dp.toPx(), center = pos)
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = pos)
            }

            // Precision Crosshair Loupe
            if (activeHandle != null) {
                val loupeCenter = if (touchPos.x > w * 0.5f) Offset(80.dp.toPx(), 80.dp.toPx()) else Offset(w - 80.dp.toPx(), 80.dp.toPx())
                val loupeRadius = 50.dp.toPx()

                drawCircle(color = Color.Black.copy(alpha = 0.6f), radius = loupeRadius + 4f, center = loupeCenter)
                drawCircle(color = Color(0xFF1E293B), radius = loupeRadius, center = loupeCenter)
                drawCircle(color = LufickEmerald, radius = loupeRadius, center = loupeCenter, style = Stroke(width = 3.dp.toPx()))

                drawLine(
                    color = Color(0xFF22D3EE),
                    start = Offset(loupeCenter.x - 20.dp.toPx(), loupeCenter.y),
                    end = Offset(loupeCenter.x + 20.dp.toPx(), loupeCenter.y),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF22D3EE),
                    start = Offset(loupeCenter.x, loupeCenter.y - 20.dp.toPx()),
                    end = Offset(loupeCenter.x, loupeCenter.y + 20.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = loupeCenter)
            }
        }
    }
}
