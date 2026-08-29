package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.util.currentTimeMillis

actual class PlatformPdfEngine {
    actual suspend fun generatePdf(
        documentTitle: String,
        pages: List<ScannedPage>,
        config: PdfConfig
    ): String {
        return "ios_document_${currentTimeMillis()}.pdf"
    }

    actual suspend fun createPdf(
        imagePaths: List<String>,
        title: String,
        config: PdfConfig
    ): String {
        return "ios_document_${currentTimeMillis()}.pdf"
    }
}

@Composable
actual fun rememberPlatformPdfEngine(): PlatformPdfEngine {
    return remember { PlatformPdfEngine() }
}
