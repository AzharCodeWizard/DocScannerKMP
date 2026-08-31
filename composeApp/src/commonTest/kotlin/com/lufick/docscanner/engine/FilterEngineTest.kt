package com.lufick.docscanner.engine

import com.lufick.docscanner.model.FilterType
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FilterEngineTest {

    @Test
    fun testColorMatrixGenerationAllPresets() {
        val presets = listOf(
            FilterType.ORIGINAL,
            FilterType.MAGIC_COLOR_1,
            FilterType.MAGIC_COLOR_2,
            FilterType.SHARP_BW,
            FilterType.GRAYSCALE,
            FilterType.ECO_PRINT
        )

        for (filter in presets) {
            val matrix = FilterEngine.getColorMatrixForFilter(
                filter = filter,
                brightness = 1.0f,
                contrast = 1.2f,
                saturation = 1.0f
            )
            assertNotNull(matrix, "Matrix for filter $filter should not be null")
            assertEquals(20, matrix.values.size, "Matrix for $filter must have 20 elements (4x5)")

            // Alpha row check (indices 15..19 -> row 4: [0, 0, 0, 1, 0])
            assertEquals(0f, matrix.values[15], "Alpha row R coefficient must be 0")
            assertEquals(0f, matrix.values[16], "Alpha row G coefficient must be 0")
            assertEquals(0f, matrix.values[17], "Alpha row B coefficient must be 0")
            assertEquals(1f, matrix.values[18], "Alpha row A coefficient must be 1")
            assertEquals(0f, matrix.values[19], "Alpha row translation must be 0")
        }
    }

    @Test
    fun testOriginalFilterIdentityMapping() {
        // Brightness = 1.0 (bShift = 0), Contrast = 1.0, Saturation = 1.0
        val matrix = FilterEngine.getColorMatrixForFilter(
            filter = FilterType.ORIGINAL,
            brightness = 1.0f,
            contrast = 1.0f,
            saturation = 1.0f
        )

        val v = matrix.values
        // Diagonal RGB should be 1.0, off-diagonal RGB should be 0.0, translation should be 0.0
        assertEquals(1.0f, v[0], 0.001f, "R->R identity")
        assertEquals(0.0f, v[1], 0.001f, "G->R identity")
        assertEquals(0.0f, v[2], 0.001f, "B->R identity")
        assertEquals(0.0f, v[4], 0.001f, "R translation identity")

        assertEquals(0.0f, v[5], 0.001f, "R->G identity")
        assertEquals(1.0f, v[6], 0.001f, "G->G identity")
        assertEquals(0.0f, v[7], 0.001f, "B->G identity")
        assertEquals(0.0f, v[9], 0.001f, "G translation identity")

        assertEquals(0.0f, v[10], 0.001f, "R->B identity")
        assertEquals(0.0f, v[11], 0.001f, "G->B identity")
        assertEquals(1.0f, v[12], 0.001f, "B->B identity")
        assertEquals(0.0f, v[14], 0.001f, "B translation identity")
    }

    @Test
    fun testBrightnessAndContrastScaling() {
        // High brightness (+50%) -> bShift = 0.5 * 255 = 127.5f
        val highBrightness = FilterEngine.getColorMatrixForFilter(
            filter = FilterType.ORIGINAL,
            brightness = 1.5f,
            contrast = 1.0f,
            saturation = 1.0f
        )
        assertEquals(127.5f, highBrightness.values[4], 0.01f, "R translation with +50% brightness")
        assertEquals(127.5f, highBrightness.values[9], 0.01f, "G translation with +50% brightness")
        assertEquals(127.5f, highBrightness.values[14], 0.01f, "B translation with +50% brightness")

        // Contrast scaling: contrast = 1.5 -> t = 128 * (1 - 1.5) = -64.0f
        val highContrast = FilterEngine.getColorMatrixForFilter(
            filter = FilterType.ORIGINAL,
            brightness = 1.0f,
            contrast = 1.5f,
            saturation = 1.0f
        )
        assertEquals(-64.0f, highContrast.values[4], 0.01f, "Translation with 1.5 contrast")
    }

    @Test
    fun testMagicColorPresets() {
        val magic1 = FilterEngine.getColorMatrixForFilter(FilterType.MAGIC_COLOR_1, 1.0f, 1.0f, 1.0f)
        val magic2 = FilterEngine.getColorMatrixForFilter(FilterType.MAGIC_COLOR_2, 1.0f, 1.0f, 1.0f)

        // Magic 1 has higher contrast boost (1.28) and whiteLift (34.0) than Magic 2 (1.12 and 16.0)
        // t = 128 * (1 - 1.28) + 0 + 34 = -35.84 + 34 = -1.84
        assertEquals(-1.84f, magic1.values[4], 0.05f)

        // Magic 2: t = 128 * (1 - 1.12) + 0 + 16 = -15.36 + 16 = 0.64
        assertEquals(0.64f, magic2.values[4], 0.05f)
    }

    @Test
    fun testSharpBwAndGrayscaleLuminanceWeights() {
        val sharpBw = FilterEngine.getColorMatrixForFilter(FilterType.SHARP_BW, 1.0f, 1.0f, 1.0f)
        val grayscale = FilterEngine.getColorMatrixForFilter(FilterType.GRAYSCALE, 1.0f, 1.0f, 1.0f)

        // Sharp BW uses Rec.601 coefficients (0.299, 0.587, 0.114) scaled by contrast (min 1.85)
        val bwContrast = 1.85f
        assertEquals(0.299f * bwContrast, sharpBw.values[0], 0.001f)
        assertEquals(0.587f * bwContrast, sharpBw.values[1], 0.001f)
        assertEquals(0.114f * bwContrast, sharpBw.values[2], 0.001f)

        // Grayscale uses contrast 1.18 * 1.0
        val grayContrast = 1.18f
        assertEquals(0.299f * grayContrast, grayscale.values[0], 0.001f)
        assertEquals(0.587f * grayContrast, grayscale.values[1], 0.001f)
        assertEquals(0.114f * grayContrast, grayscale.values[2], 0.001f)
    }

    @Test
    fun testEcoPrintFilter() {
        val eco = FilterEngine.getColorMatrixForFilter(FilterType.ECO_PRINT, 1.0f, 1.0f, 1.0f)
        // Eco Print uses 0.25, 0.50, 0.10 scaled by 1.05
        val ecoC = 1.05f
        assertEquals(0.25f * ecoC, eco.values[0], 0.001f)
        assertEquals(0.50f * ecoC, eco.values[1], 0.001f)
        assertEquals(0.10f * ecoC, eco.values[2], 0.001f)
        // White lift is 56.0: t = 128 * (1 - 1.05) + 56 = -6.4 + 56 = 49.6
        assertEquals(49.6f, eco.values[4], 0.05f)
    }

    @Test
    fun testColorMatrixValidation() {
        assertFailsWith<IllegalArgumentException> {
            FilterEngine.ColorMatrix(FloatArray(19))
        }
        assertFailsWith<IllegalArgumentException> {
            FilterEngine.ColorMatrix(FloatArray(21))
        }
        val valid = FilterEngine.ColorMatrix(FloatArray(20))
        assertEquals(20, valid.values.size)
    }

    @Test
    fun testPixelBufferProcessing() {
        // Pure White, Pure Black, Pure Red, Pure Green, Pure Blue
        val pixels = intArrayOf(
            0xFFFFFFFF.toInt(),
            0xFF000000.toInt(),
            0xFFFF0000.toInt(),
            0xFF00FF00.toInt(),
            0xFF0000FF.toInt()
        )

        val processed = FilterEngine.processPixelBuffer(
            pixels = pixels,
            width = 5,
            height = 1,
            filter = FilterType.SHARP_BW,
            brightness = 1.0f,
            contrast = 1.5f,
            saturation = 1.0f
        )

        assertEquals(5, processed.size)

        // Verify alpha channel (0xFF) is strictly preserved for all processed pixels
        for (p in processed) {
            val alpha = (p ushr 24) and 0xFF
            assertEquals(0xFF, alpha, "Alpha channel must remain 0xFF")

            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF
            assertTrue(r in 0..255, "R channel ($r) must be in 0..255")
            assertTrue(g in 0..255, "G channel ($g) must be in 0..255")
            assertTrue(b in 0..255, "B channel ($b) must be in 0..255")
        }
    }

    @Test
    fun testEmptyPixelBuffer() {
        val empty = IntArray(0)
        val result = FilterEngine.processPixelBuffer(
            pixels = empty,
            width = 0,
            height = 0,
            filter = FilterType.ORIGINAL,
            brightness = 1.0f,
            contrast = 1.0f
        )
        assertEquals(0, result.size)
    }
}
