package com.lufick.docscanner.platform

import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.ScannedPage

actual class PlatformPdfEngine {

    actual suspend fun generatePdf(
        documentTitle: String,
        pages: List<ScannedPage>,
        config: PdfConfig
    ): String {
        return "ios_${documentTitle.replace(" ", "_")}.pdf"
    }
}
