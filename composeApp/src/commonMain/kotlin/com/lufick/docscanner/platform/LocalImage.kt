package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter

@Composable
expect fun LocalImage(
    path: String,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
    rotationDegrees: Int = 0
)
