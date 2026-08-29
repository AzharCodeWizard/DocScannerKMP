package com.lufick.docscanner.platform

import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.ScannedPage

expect class PlatformPdfEngine {
    suspend fun generatePdf(
        documentTitle: String,
        pages: List<ScannedPage>,
        config: PdfConfig
    ): String // Returns output file path
}
