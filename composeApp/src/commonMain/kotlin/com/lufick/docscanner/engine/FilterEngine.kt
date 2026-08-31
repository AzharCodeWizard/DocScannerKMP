package com.lufick.docscanner.engine

import com.lufick.docscanner.model.FilterType
import kotlin.math.max
import kotlin.math.min

/**
 * Multiplatform Document Image Enhancement & Color Processing Engine.
 * Implements Adobe Scan & CamScanner grade color transformation algorithms.
 */
object FilterEngine {

    data class ColorMatrix(val values: FloatArray) {
        init {
            require(values.size == 20) { "ColorMatrix must have 20 elements (4x5 matrix)" }
        }
    }

    /**
     * Generate 4x5 ColorMatrix for GPU / Canvas rendering corresponding to FilterType and enhancement parameters.
     */
    fun getColorMatrixForFilter(
        filter: FilterType,
        brightness: Float = 1.0f,
        contrast: Float = 1.2f,
        saturation: Float = 1.0f
    ): ColorMatrix {
        val bShift = (brightness - 1.0f) * 255.0f

        // Standard Rec. 709 Luminance coefficients
        val lr = 0.2126f
        val lg = 0.7152f
        val lb = 0.0722f

        return when (filter) {
            FilterType.ORIGINAL -> {
                val s = saturation.coerceAtLeast(0f)
                val c = contrast
                val t = 128f * (1f - c) + bShift

                val rSat = lr * (1 - s)
                val gSat = lg * (1 - s)
                val bSat = lb * (1 - s)

                ColorMatrix(floatArrayOf(
                    (rSat + s) * c, gSat * c, bSat * c, 0f, t,
                    rSat * c, (gSat + s) * c, bSat * c, 0f, t,
                    rSat * c, gSat * c, (bSat + s) * c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.MAGIC_COLOR_1 -> {
                // Signature Magic Color: High dynamic contrast, vivid ink saturation & pure paper whitening
                val s = 1.38f * saturation
                val c = 1.28f * contrast
                val whiteLift = 34.0f
                val t = 128f * (1f - c) + bShift + whiteLift

                val rSat = lr * (1 - s)
                val gSat = lg * (1 - s)
                val bSat = lb * (1 - s)

                ColorMatrix(floatArrayOf(
                    (rSat + s) * c, gSat * c, bSat * c, 0f, t,
                    rSat * c, (gSat + s) * c, bSat * c, 0f, t,
                    rSat * c, gSat * c, (bSat + s) * c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.MAGIC_COLOR_2 -> {
                // Soft Magic Color: Natural magazine/photographic color balance with mild shadow clearing
                val s = 1.16f * saturation
                val c = 1.12f * contrast
                val whiteLift = 16.0f
                val t = 128f * (1f - c) + bShift + whiteLift

                val rSat = lr * (1 - s)
                val gSat = lg * (1 - s)
                val bSat = lb * (1 - s)

                ColorMatrix(floatArrayOf(
                    (rSat + s) * c, gSat * c, bSat * c, 0f, t,
                    rSat * c, (gSat + s) * c, bSat * c, 0f, t,
                    rSat * c, gSat * c, (bSat + s) * c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.SHARP_BW -> {
                // B&W Document: Sharp binarized text contrast with pure #FFFFFF background
                val c = max(1.85f, contrast * 1.65f)
                val blackThreshold = -36.0f
                val t = 128f * (1f - c) + bShift + blackThreshold

                val rBw = 0.299f * c
                val gBw = 0.587f * c
                val bBw = 0.114f * c

                ColorMatrix(floatArrayOf(
                    rBw, gBw, bBw, 0f, t,
                    rBw, gBw, bBw, 0f, t,
                    rBw, gBw, bBw, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.GRAYSCALE -> {
                // Smooth Grayscale: High-fidelity monochrome with background noise suppression
                val c = 1.18f * contrast
                val whiteLift = 22.0f
                val t = 128f * (1f - c) + bShift + whiteLift

                val rGray = 0.299f * c
                val gGray = 0.587f * c
                val bGray = 0.114f * c

                ColorMatrix(floatArrayOf(
                    rGray, gGray, bGray, 0f, t,
                    rGray, gGray, bGray, 0f, t,
                    rGray, gGray, bGray, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.SUPER_CLEAN -> {
                // Super Clean: Shadow & yellow stain suppression with bright white background
                val s = 1.15f * saturation
                val c = 1.35f * contrast
                val whiteLift = 44.0f
                val t = 128f * (1f - c) + bShift + whiteLift

                val rSat = lr * (1 - s)
                val gSat = lg * (1 - s)
                val bSat = lb * (1 - s)

                ColorMatrix(floatArrayOf(
                    (rSat + s) * c, gSat * c, bSat * c, 0f, t,
                    rSat * c, (gSat + s) * c, bSat * c, 0f, t,
                    rSat * c, gSat * c, (bSat + s) * c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.VIVID_PHOTO -> {
                // Vivid Photo: Rich saturation & enhanced color dynamic range for graphics/photos
                val s = 1.55f * saturation
                val c = 1.22f * contrast
                val whiteLift = 10.0f
                val t = 128f * (1f - c) + bShift + whiteLift

                val rSat = lr * (1 - s)
                val gSat = lg * (1 - s)
                val bSat = lb * (1 - s)

                ColorMatrix(floatArrayOf(
                    (rSat + s) * c, gSat * c, bSat * c, 0f, t,
                    rSat * c, (gSat + s) * c, bSat * c, 0f, t,
                    rSat * c, gSat * c, (bSat + s) * c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            FilterType.ECO_PRINT -> {
                // Eco Print: Toner/Ink saver mode with elevated white floor
                val c = 1.05f * contrast
                val whiteLift = 56.0f
                val t = 128f * (1f - c) + bShift + whiteLift

                val rEco = 0.25f * c
                val gEco = 0.50f * c
                val bEco = 0.10f * c

                ColorMatrix(floatArrayOf(
                    rEco, gEco, bEco, 0f, t,
                    rEco, gEco, bEco, 0f, t,
                    rEco, gEco, bEco, 0f, t,
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
        contrast: Float,
        saturation: Float = 1.0f
    ): IntArray {
        val output = IntArray(pixels.size)
        val matrix = getColorMatrixForFilter(filter, brightness, contrast, saturation).values

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

