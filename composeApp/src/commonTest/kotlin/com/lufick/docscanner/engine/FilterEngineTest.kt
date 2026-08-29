package com.lufick.docscanner.engine

import com.lufick.docscanner.model.FilterType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FilterEngineTest {

    @Test
    fun testColorMatrixGeneration() {
        FilterType.entries.forEach { filter ->
            val matrix = FilterEngine.getColorMatrixForFilter(filter, brightness = 1.0f, contrast = 1.2f)
            assertNotNull(matrix)
            assertEquals(20, matrix.values.size)
        }
    }

    @Test
    fun testPixelBufferProcessing() {
        val pixels = intArrayOf(
            0xFFFFFFFF.toInt(), // White
            0xFF000000.toInt(), // Black
            0xFFFF0000.toInt()  // Red
        )
        val processed = FilterEngine.processPixelBuffer(
            pixels, 3, 1, FilterType.SHARP_BW, 1.0f, 1.5f
        )
        assertEquals(3, processed.size)
    }
}
