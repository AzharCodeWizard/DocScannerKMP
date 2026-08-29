package com.lufick.docscanner.platform

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

    actual suspend fun stitchIdCard(
        frontImagePath: String,
        backImagePath: String
    ): String {
        return "ios_id_card_${System.currentTimeMillis()}.jpg"
    }
}
