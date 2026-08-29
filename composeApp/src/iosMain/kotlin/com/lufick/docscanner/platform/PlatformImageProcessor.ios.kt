package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.QuadCorners

actual class PlatformImageProcessor {

    actual suspend fun applyPerspectiveWarp(
        sourceImagePath: String,
        corners: QuadCorners,
        rotationDegrees: Int
    ): String {
        return "ios_warped_${System.currentTimeMillis()}.jpg"
    }

    actual suspend fun applyFilter(
        imagePath: String,
        filter: FilterType,
        brightness: Float,
        contrast: Float
    ): String {
        return "ios_filtered_${System.currentTimeMillis()}.jpg"
    }

    actual suspend fun detectDocumentCorners(imagePath: String): QuadCorners {
        return QuadCorners(
            topLeft = PointF(0.08f, 0.08f),
            topRight = PointF(0.92f, 0.08f),
            bottomRight = PointF(0.92f, 0.92f),
            bottomLeft = PointF(0.08f, 0.92f)
        )
    }

    actual suspend fun stitchIdCard(
        frontImagePath: String,
        backImagePath: String
    ): String {
        return "ios_id_card_${System.currentTimeMillis()}.jpg"
    }
}

@Composable
actual fun rememberPlatformImageProcessor(): PlatformImageProcessor {
    return remember { PlatformImageProcessor() }
}
