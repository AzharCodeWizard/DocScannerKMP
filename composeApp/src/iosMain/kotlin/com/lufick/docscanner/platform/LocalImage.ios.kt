package com.lufick.docscanner.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter

@Composable
actual fun LocalImage(
    path: String,
    modifier: Modifier,
    colorFilter: ColorFilter?,
    rotationDegrees: Int
) {
    // Stub for iOS
    Box(modifier = modifier.background(Color.DarkGray))
}

