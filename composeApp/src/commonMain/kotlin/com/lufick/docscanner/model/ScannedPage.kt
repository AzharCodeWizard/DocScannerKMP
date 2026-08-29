package com.lufick.docscanner.model

import kotlinx.serialization.Serializable

@Serializable
data class PointF(val x: Float, val y: Float)

@Serializable
data class QuadCorners(
    val topLeft: PointF = PointF(0.08f, 0.08f),
    val topRight: PointF = PointF(0.92f, 0.08f),
    val bottomRight: PointF = PointF(0.92f, 0.92f),
    val bottomLeft: PointF = PointF(0.08f, 0.92f)
)

@Serializable
data class ScannedPage(
    val id: String,
    val pageNumber: Int,
    val originalImagePath: String,
    val processedImagePath: String,
    val thumbnailPath: String = "",
    val cropCorners: QuadCorners = QuadCorners(),
    val rotationDegrees: Int = 0,
    val filterType: FilterType = FilterType.MAGIC_COLOR_1,
    val brightness: Float = 1.0f,
    val contrast: Float = 1.2f,
    val ocrText: String? = null,
    val createdAt: Long = 0L
)
