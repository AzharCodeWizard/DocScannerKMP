package com.lufick.docscanner.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun LocalImage(path: String, modifier: Modifier) {
    // Stub for iOS
    Box(modifier = modifier.background(Color.DarkGray))
}
