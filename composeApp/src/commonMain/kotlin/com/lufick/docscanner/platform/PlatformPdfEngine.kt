package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.ScannedPage

expect class PlatformPdfEngine {
    suspend fun generatePdf(
        documentTitle: String,
        pages: List<ScannedPage>,
        config: PdfConfig
    ): String

    suspend fun createPdf(
        imagePaths: List<String>,
        title: String,
        config: PdfConfig
    ): String
}

@Composable
expect fun rememberPlatformPdfEngine(): PlatformPdfEngine
