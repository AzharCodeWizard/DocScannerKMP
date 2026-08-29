package com.lufick.docscanner.platform

import com.lufick.docscanner.model.OcrResult

expect class PlatformOcrEngine {
    suspend fun recognizeText(imagePath: String): OcrResult
}
