package com.lufick.docscanner.model

import kotlinx.serialization.Serializable

@Serializable
enum class ScanMode(val title: String, val description: String) {
    DOCUMENT("Document", "Auto edge-detection for receipts, invoices & documents"),
    ID_CARD("ID Card", "Scan Front & Back on single page with guide frames"),
    BOOK("Book", "Dual page capture with automated left/right split"),
    PASSPORT("Passport", "Standard passport frame with MRZ detection"),
    QR_CODE("QR & Barcode", "Instant barcode / QR decoding")
}

@Serializable
enum class FilterType(val displayName: String, val description: String) {
    ORIGINAL("Original", "Raw unprocessed capture"),
    MAGIC_COLOR_1("Magic Color", "Vivid contrast with paper whitening"),
    MAGIC_COLOR_2("Magic Pro", "Natural tone with shadow clearing"),
    SUPER_CLEAN("Super Clean", "Removes shadows & yellow paper tint"),
    SHARP_BW("Sharp B&W", "High contrast black and white text"),
    GRAYSCALE("Grayscale", "Smooth monochrome with noise removal"),
    VIVID_PHOTO("Vivid Color", "Rich saturation for color graphics"),
    ECO_PRINT("Eco Print", "Ink & toner saver mode")
}

@Serializable
enum class PageSize(val displayName: String, val widthPt: Float, val heightPt: Float) {
    A4("A4 (210 x 297 mm)", 595.28f, 841.89f),
    LETTER("US Letter (8.5 x 11 in)", 612.0f, 792.0f),
    LEGAL("US Legal (8.5 x 14 in)", 612.0f, 1008.0f),
    A3("A3 (297 x 420 mm)", 841.89f, 1190.55f),
    A5("A5 (148 x 210 mm)", 419.53f, 595.28f),
    BUSINESS_CARD("Card (3.5 x 2 in)", 252.0f, 144.0f),
    FIT_IMAGE("Fit to Image", 0f, 0f)
}

@Serializable
enum class PageOrientation {
    AUTO,
    PORTRAIT,
    LANDSCAPE
}

@Serializable
enum class PdfQuality(val displayName: String, val compressionRatio: Float, val targetDpi: Int) {
    HIGH("High Quality", 0.90f, 300),
    BALANCED("Balanced", 0.70f, 200),
    LOW("Small Size (Low)", 0.45f, 150)
}
