package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class PlatformShare {
    actual fun shareFile(filePath: String, mimeType: String) {
        // UIActivityViewController presentation on iOS
    }

    actual fun shareText(text: String) {
        // UIActivityViewController presentation on iOS
    }

    actual fun printDocument(filePath: String) {
        // UIPrintInteractionController on iOS
    }
}

@Composable
actual fun rememberPlatformShare(): PlatformShare {
    return remember { PlatformShare() }
}
