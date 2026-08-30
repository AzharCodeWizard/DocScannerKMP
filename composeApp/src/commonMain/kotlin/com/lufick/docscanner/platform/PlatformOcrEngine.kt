package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import com.lufick.docscanner.model.OcrResult

expect class PlatformOcrEngine {
    suspend fun recognizeText(imagePath: String): OcrResult
}

@Composable
expect fun rememberPlatformOcrEngine(): PlatformOcrEngine

