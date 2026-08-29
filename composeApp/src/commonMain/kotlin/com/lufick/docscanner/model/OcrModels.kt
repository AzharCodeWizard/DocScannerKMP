package com.lufick.docscanner.model

import kotlinx.serialization.Serializable

@Serializable
data class OcrBoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

@Serializable
data class OcrLine(
    val text: String,
    val boundingBox: OcrBoundingBox,
    val confidence: Float = 1.0f
)

@Serializable
data class OcrBlock(
    val text: String,
    val lines: List<OcrLine> = emptyList(),
    val boundingBox: OcrBoundingBox
)

@Serializable
data class ExtractedEntity(
    val key: String,
    val value: String,
    val category: String,
    val confidence: Float = 0.95f
)

@Serializable
data class OcrResult(
    val fullText: String,
    val blocks: List<OcrBlock> = emptyList(),
    val entities: List<ExtractedEntity> = emptyList(),
    val detectedLanguage: String = "en",
    val processingTimeMs: Long = 0L
)
