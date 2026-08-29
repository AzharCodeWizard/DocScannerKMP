package com.lufick.docscanner.engine

import com.lufick.docscanner.model.FilterType
import kotlin.math.max
import kotlin.math.min

/**
 * Multiplatform Document Image Enhancement & Color Processing Engine.
 */
object FilterEngine {

    data class ColorMatrix(val values: FloatArray) {
        init {
            require(values.size == 20) { "ColorMatrix must have 20 elements (4x5 matrix)" }
        }
    }

    /**
     * Generate 4x5 ColorMatrix for GPU / Canvas rendering corresponding to FilterType.
     */
    fun getColorMatrixForFilter(filter: FilterType, brightness: Float = 1.0f, contrast: Float = 1.2f): ColorMatrix {
        val bShift = (brightness - 1.0f) * 255.0f
        val c = contrast

        return when (filter) {
            FilterType.ORIGINAL -> {
                ColorMatrix(floatArrayOf(
                    1f, 0f, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.MAGIC_COLOR_1 -> {
                // Signature Lufick Magic Color: Contrast boost, saturation pop, white level lift
                val sat = 1.35f
                val rSat = 0.213f * (1 - sat)
                val gSat = 0.715f * (1 - sat)
                val bSat = 0.072f * (1 - sat)

                ColorMatrix(floatArrayOf(
                    (rSat + sat) * c, gSat * c, bSat * c, 0f, bShift + 12f,
                    rSat * c, (gSat + sat) * c, bSat * c, 0f, bShift + 12f,
                    rSat * c, gSat * c, (bSat + sat) * c, 0f, bShift + 12f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.MAGIC_COLOR_2 -> {
                // Softer color balance for photos & magazine covers
                val sat = 1.15f
                val rSat = 0.213f * (1 - sat)
                val gSat = 0.715f * (1 - sat)
                val bSat = 0.072f * (1 - sat)

                ColorMatrix(floatArrayOf(
                    (rSat + sat) * 1.1f, gSat * 1.1f, bSat * 1.1f, 0f, bShift + 6f,
                    rSat * 1.1f, (gSat + sat) * 1.1f, bSat * 1.1f, 0f, bShift + 6f,
                    rSat * 1.1f, gSat * 1.1f, (bSat + sat) * 1.1f, 0f, bShift + 6f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.SHARP_BW -> {
                // High-contrast clean black & white binarization
                val bwContrast = max(1.6f, c * 1.5f)
                ColorMatrix(floatArrayOf(
                    0.299f * bwContrast, 0.587f * bwContrast, 0.114f * bwContrast, 0f, bShift - 40f,
                    0.299f * bwContrast, 0.587f * bwContrast, 0.114f * bwContrast, 0f, bShift - 40f,
                    0.299f * bwContrast, 0.587f * bwContrast, 0.114f * bwContrast, 0f, bShift - 40f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.GRAYSCALE -> {
                // Smooth grays with noise suppression
                ColorMatrix(floatArrayOf(
                    0.299f * c, 0.587f * c, 0.114f * c, 0f, bShift + 10f,
                    0.299f * c, 0.587f * c, 0.114f * c, 0f, bShift + 10f,
                    0.299f * c, 0.587f * c, 0.114f * c, 0f, bShift + 10f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.ECO_PRINT -> {
                // Eco Print ink-saver
                ColorMatrix(floatArrayOf(
                    0.25f * c, 0.5f * c, 0.1f * c, 0f, bShift + 30f,
                    0.25f * c, 0.5f * c, 0.1f * c, 0f, bShift + 30f,
                    0.25f * c, 0.5f * c, 0.1f * c, 0f, bShift + 30f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
        }
    }

    /**
     * Pure Kotlin pixel-level RGB processor for memory buffers.
     */
    fun processPixelBuffer(
        pixels: IntArray,
        width: Int,
        height: Int,
        filter: FilterType,
        brightness: Float,
        contrast: Float
    ): IntArray {
        val output = IntArray(pixels.size)
        val matrix = getColorMatrixForFilter(filter, brightness, contrast).values

        for (i in pixels.indices) {
            val color = pixels[i]
            val a = (color ushr 24) and 0xFF
            val r = (color ushr 16) and 0xFF
            val g = (color ushr 8) and 0xFF
            val b = color and 0xFF

            val newR = clamp((r * matrix[0] + g * matrix[1] + b * matrix[2] + matrix[4]).toInt())
            val newG = clamp((r * matrix[5] + g * matrix[6] + b * matrix[7] + matrix[9]).toInt())
            val newB = clamp((r * matrix[10] + g * matrix[11] + b * matrix[12] + matrix[14]).toInt())

            output[i] = (a shl 24) or (newR shl 16) or (newG shl 8) or newB
        }
        return output
    }

    private fun clamp(v: Int): Int = min(255, max(0, v))
}
