package com.lufick.docscanner.model

import kotlinx.serialization.Serializable

@Serializable
data class WatermarkConfig(
    val isEnabled: Boolean = false,
    val text: String = "CONFIDENTIAL",
    val opacity: Float = 0.25f,
    val rotationDegrees: Float = 45f,
    val fontSizePt: Float = 42f,
    val colorHex: String = "#94A3B8"
)

@Serializable
data class SignatureData(
    val isEnabled: Boolean = false,
    val points: List<PointF> = emptyList(),
    val signatureImagePath: String? = null,
    val posXNormalized: Float = 0.7f,
    val posYNormalized: Float = 0.85f,
    val widthNormalized: Float = 0.25f,
    val heightNormalized: Float = 0.1f
)

@Serializable
data class PdfConfig(
    val pageSize: PageSize = PageSize.A4,
    val orientation: PageOrientation = PageOrientation.AUTO,
    val quality: PdfQuality = PdfQuality.BALANCED,
    val watermark: WatermarkConfig = WatermarkConfig(),
    val signature: SignatureData = SignatureData(),
    val passwordProtection: String? = null,
    val includeOcrSearchLayer: Boolean = true,
    val addPageNumbers: Boolean = true
)
