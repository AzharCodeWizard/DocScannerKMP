package com.lufick.docscanner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.theme.LufickEmerald

@Composable
fun ShutterButton(
    modifier: Modifier = Modifier,
    isAutoCapture: Boolean = false,
    autoCaptureProgress: Float = 0f, // 0..1
    isBatchMode: Boolean = false,
    batchCount: Int = 0,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(120)
    )

    Box(
        modifier = modifier
            .size(84.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer Progress Arc Canvas (Countdown for Auto-Capture)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.5.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f

            // Base Outer Ring
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Auto-Capture Green Progress Arc
            if (isAutoCapture && autoCaptureProgress > 0f) {
                drawArc(
                    color = LufickEmerald,
                    startAngle = -90f,
                    sweepAngle = autoCaptureProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth + 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Inner White Shutter Core
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isAutoCapture) LufickEmerald else Color.White)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isBatchMode && batchCount > 0) {
                Text(
                    text = "+$batchCount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAutoCapture) Color.Black else Color.DarkGray
                )
            }
        }
    }
}
