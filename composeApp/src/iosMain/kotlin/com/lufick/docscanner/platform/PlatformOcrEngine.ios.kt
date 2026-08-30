package com.lufick.docscanner.platform

import com.lufick.docscanner.model.OcrResult

actual class PlatformOcrEngine {

    actual suspend fun recognizeText(imagePath: String): OcrResult {
        return OcrResult(
            fullText = "",
            blocks = emptyList(),
            entities = emptyList(),
            detectedLanguage = "en",
            processingTimeMs = 0L
        )
    }
}

@androidx.compose.runtime.Composable
actual fun rememberPlatformOcrEngine(): PlatformOcrEngine {
    return androidx.compose.runtime.remember { PlatformOcrEngine() }
}


