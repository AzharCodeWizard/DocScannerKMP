package com.lufick.docscanner.platform

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
