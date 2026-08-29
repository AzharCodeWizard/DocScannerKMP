package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable

import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.QuadCorners

expect class PlatformImageProcessor {
    suspend fun applyPerspectiveWarp(
        sourceImagePath: String,
        corners: QuadCorners,
        rotationDegrees: Int
    ): String

    suspend fun applyFilter(
        imagePath: String,
        filter: FilterType,
        brightness: Float,
        contrast: Float
    ): String

    suspend fun stitchIdCard(
        frontImagePath: String,
        backImagePath: String
    ): String
}



@Composable
expect fun rememberPlatformImageProcessor(): PlatformImageProcessor
