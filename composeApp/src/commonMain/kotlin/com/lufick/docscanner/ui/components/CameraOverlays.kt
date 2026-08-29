package com.lufick.docscanner.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.theme.LaserScanColor
import com.lufick.docscanner.theme.LufickCyan
import com.lufick.docscanner.theme.LufickEmerald
import kotlin.math.sin

/**
 * 1. Dynamic Live Document Quad Overlay with Animated Target Reticles, Laser Sweep & Breathing Edge Tracking
 */
@Composable
fun DocumentQuadOverlay(
    modifier: Modifier = Modifier,
    quad: QuadCorners,
    isDetected: Boolean = true,
    showLaser: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Continuous Laser Sweep Animation
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Dynamic Corner Pulse & Seeking Motion
    val cornerPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val breathingWave by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Apply dynamic subtle tracking movement to corners
        val tl = Offset(quad.topLeft.x * w + breathingWave, quad.topLeft.y * h - breathingWave)
        val tr = Offset(quad.topRight.x * w - breathingWave, quad.topRight.y * h + breathingWave)
        val br = Offset(quad.bottomRight.x * w + breathingWave, quad.bottomRight.y * h + breathingWave)
        val bl = Offset(quad.bottomLeft.x * w - breathingWave, quad.bottomLeft.y * h - breathingWave)

        val quadPath = Path().apply {
            moveTo(tl.x, tl.y)
            lineTo(tr.x, tr.y)
            lineTo(br.x, br.y)
            lineTo(bl.x, bl.y)
            close()
        }

        // Semi-transparent surface tint
        drawPath(
            path = quadPath,
            color = LufickEmerald.copy(alpha = if (isDetected) 0.12f else 0.04f)
        )

        // Glowing Boundary Line
        drawPath(
            path = quadPath,
            color = LufickEmerald.copy(alpha = 0.85f),
            style = Stroke(
                width = 2.5.dp.toPx(),
                pathEffect = if (isDetected) null else PathEffect.dashPathEffect(floatArrayOf(20f, 15f))
            )
        )

        // 4 Dynamic L-Bracket Corner Targeting Reticles
        val bracketLen = (28.dp.toPx() * cornerPulse).coerceIn(24.dp.toPx(), 36.dp.toPx())
        val bracketStroke = 4.5.dp.toPx()
        val cornerColor = if (isDetected) LufickEmerald else LufickCyan

        // Top-Left L
        drawLine(cornerColor, tl, Offset(tl.x + bracketLen, tl.y), strokeWidth = bracketStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, tl, Offset(tl.x, tl.y + bracketLen), strokeWidth = bracketStroke, cap = StrokeCap.Round)

        // Top-Right L
        drawLine(cornerColor, tr, Offset(tr.x - bracketLen, tr.y), strokeWidth = bracketStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, tr, Offset(tr.x, tr.y + bracketLen), strokeWidth = bracketStroke, cap = StrokeCap.Round)

        // Bottom-Right L
        drawLine(cornerColor, br, Offset(br.x - bracketLen, br.y), strokeWidth = bracketStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, br, Offset(br.x, br.y - bracketLen), strokeWidth = bracketStroke, cap = StrokeCap.Round)

        // Bottom-Left L
        drawLine(cornerColor, bl, Offset(bl.x + bracketLen, bl.y), strokeWidth = bracketStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, bl, Offset(bl.x, bl.y - bracketLen), strokeWidth = bracketStroke, cap = StrokeCap.Round)

        // 4 Glowing Center Dots at Corners
        listOf(tl, tr, br, bl).forEach { pt ->
            drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = pt)
        }

        // Sweeping Laser Beam
        if (showLaser && isDetected) {
            val currentLaserY = (tl.y + (bl.y - tl.y) * laserProgress).coerceIn(0f, h)
            val laserStartX = tl.x + (bl.x - tl.x) * laserProgress
            val laserEndX = tr.x + (br.x - tr.x) * laserProgress

            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        LaserScanColor.copy(alpha = 0.8f),
                        Color.White,
                        LaserScanColor.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                ),
                start = Offset(laserStartX, currentLaserY),
                end = Offset(laserEndX, currentLaserY),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 2. ID Card Guide Cutout Overlay
 */
@Composable
fun IdCardGuideOverlay(
    modifier: Modifier = Modifier,
    isFrontSide: Boolean = true
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
    ) {
        val w = size.width
        val h = size.height

        drawRect(Color.Black.copy(alpha = 0.65f))

        val cardWidth = (w * 0.88f).coerceAtMost(360.dp.toPx())
        val cardHeight = cardWidth / 1.586f
        val cardLeft = (w - cardWidth) / 2f
        val cardTop = (h - cardHeight) / 2f - 40.dp.toPx()

        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            color = LufickEmerald,
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 2.5.dp.toPx())
        )

        val photoW = cardWidth * 0.28f
        val photoH = cardHeight * 0.52f
        val photoLeft = cardLeft + 20.dp.toPx()
        val photoTop = cardTop + (cardHeight - photoH) / 2f

        drawRoundRect(
            color = LufickEmerald.copy(alpha = 0.4f),
            topLeft = Offset(photoLeft, photoTop),
            size = Size(photoW, photoH),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
        )

        val lineLeft = photoLeft + photoW + 16.dp.toPx()
        val lineWidth = cardWidth - (lineLeft - cardLeft) - 20.dp.toPx()
        val lineStartY = photoTop + 8.dp.toPx()

        for (i in 0..2) {
            val y = lineStartY + i * 22.dp.toPx()
            drawLine(
                color = LufickEmerald.copy(alpha = 0.3f),
                start = Offset(lineLeft, y),
                end = Offset(lineLeft + lineWidth * (if (i == 2) 0.6f else 0.9f), y),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
            )
        }
    }
}

/**
 * 3. Passport Guide Overlay
 */
@Composable
fun PassportGuideOverlay(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
    ) {
        val w = size.width
        val h = size.height

        drawRect(Color.Black.copy(alpha = 0.65f))

        val passportWidth = (w * 0.88f).coerceAtMost(360.dp.toPx())
        val passportHeight = passportWidth * 1.42f
        val pLeft = (w - passportWidth) / 2f
        val pTop = (h - passportHeight) / 2f - 30.dp.toPx()

        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(pLeft, pTop),
            size = Size(passportWidth, passportHeight),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        drawRoundRect(
            color = LufickCyan,
            topLeft = Offset(pLeft, pTop),
            size = Size(passportWidth, passportHeight),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
            style = Stroke(width = 2.5.dp.toPx())
        )

        val mrzHeight = passportHeight * 0.22f
        val mrzTop = pTop + passportHeight - mrzHeight

        drawLine(
            color = LufickCyan.copy(alpha = 0.7f),
            start = Offset(pLeft, mrzTop),
            end = Offset(pLeft + passportWidth, mrzTop),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f))
        )
    }
}

/**
 * 4. Book Guide Overlay
 */
@Composable
fun BookGuideOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f

        drawLine(
            color = LufickEmerald,
            start = Offset(centerX, h * 0.15f),
            end = Offset(centerX, h * 0.82f),
            strokeWidth = 3.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
        )

        drawRoundRect(
            color = LufickEmerald.copy(alpha = 0.4f),
            topLeft = Offset(24.dp.toPx(), h * 0.18f),
            size = Size(centerX - 36.dp.toPx(), h * 0.60f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawRoundRect(
            color = LufickEmerald.copy(alpha = 0.4f),
            topLeft = Offset(centerX + 12.dp.toPx(), h * 0.18f),
            size = Size(centerX - 36.dp.toPx(), h * 0.60f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

/**
 * 5. QR Code Reticle Overlay
 */
@Composable
fun QrScannerOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 0.99f)
    ) {
        val w = size.width
        val h = size.height

        drawRect(Color.Black.copy(alpha = 0.6f))

        val boxSize = 250.dp.toPx()
        val left = (w - boxSize) / 2f
        val top = (h - boxSize) / 2f - 40.dp.toPx()

        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        val len = 34.dp.toPx()
        val stroke = 4.5.dp.toPx()
        val c = LufickEmerald

        drawLine(c, Offset(left, top), Offset(left + len, top), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(c, Offset(left, top), Offset(left, top + len), strokeWidth = stroke, cap = StrokeCap.Round)

        drawLine(c, Offset(left + boxSize, top), Offset(left + boxSize - len, top), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(c, Offset(left + boxSize, top), Offset(left + boxSize, top + len), strokeWidth = stroke, cap = StrokeCap.Round)

        drawLine(c, Offset(left + boxSize, top + boxSize), Offset(left + boxSize - len, top + boxSize), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(c, Offset(left + boxSize, top + boxSize), Offset(left + boxSize, top + boxSize - len), strokeWidth = stroke, cap = StrokeCap.Round)

        drawLine(c, Offset(left, top + boxSize), Offset(left + len, top + boxSize), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(c, Offset(left, top + boxSize), Offset(left, top + boxSize - len), strokeWidth = stroke, cap = StrokeCap.Round)

        val scanLineY = top + boxSize * laserY
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, LaserScanColor, Color.White, LaserScanColor, Color.Transparent)
            ),
            start = Offset(left + 8.dp.toPx(), scanLineY),
            end = Offset(left + boxSize - 8.dp.toPx(), scanLineY),
            strokeWidth = 3.dp.toPx()
        )
    }
}

/**
 * 6. Rule-of-Thirds Grid
 */
@Composable
fun RuleOfThirdsGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val gridColor = Color.White.copy(alpha = 0.22f)
        val stroke = 1.dp.toPx()

        drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = stroke)
        drawLine(gridColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth = stroke)

        drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = stroke)
        drawLine(gridColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth = stroke)
    }
}

/**
 * 7. Tap-to-Focus Ring
 */
@Composable
fun TapToFocusRing(
    point: Offset?,
    modifier: Modifier = Modifier
) {
    if (point == null) return

    val infiniteTransition = rememberInfiniteTransition()
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = 32.dp.toPx() * ringScale
        drawCircle(
            color = LufickEmerald,
            radius = radius,
            center = point,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = LufickEmerald,
            radius = 3.dp.toPx(),
            center = point
        )
    }
}
