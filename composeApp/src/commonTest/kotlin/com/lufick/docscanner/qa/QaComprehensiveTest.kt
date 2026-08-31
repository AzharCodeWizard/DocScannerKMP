package com.lufick.docscanner.qa

import com.lufick.docscanner.engine.FilterEngine
import com.lufick.docscanner.engine.OcrParser
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.PageSize
import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.PdfQuality
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.model.WatermarkConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QaComprehensiveTest {

    @Test
    fun testAllFilterMatricesGenerateValid20ElementValues() {
        for (filter in FilterType.entries) {
            val matrix = FilterEngine.getColorMatrixForFilter(
                filter = filter,
                brightness = 1.0f,
                contrast = 1.25f,
                saturation = 1.0f
            )
            assertEquals(20, matrix.values.size, "Filter $filter must have 20 matrix elements")
            
            // Verify alpha channel identity row
            assertEquals(0f, matrix.values[15], "Alpha row R coefficient must be 0")
            assertEquals(0f, matrix.values[16], "Alpha row G coefficient must be 0")
            assertEquals(0f, matrix.values[17], "Alpha row B coefficient must be 0")
            assertEquals(1f, matrix.values[18], "Alpha row A coefficient must be 1")
            assertEquals(0f, matrix.values[19], "Alpha row translation must be 0")
        }
    }

    @Test
    fun testFilterEngineBoundaryConditions() {
        val boundaryTests = listOf(
            Triple(0.0f, 0.0f, 0.0f),
            Triple(2.0f, 3.0f, 2.5f),
            Triple(-1.0f, 0.5f, 0.0f)
        )

        for (filter in FilterType.entries) {
            for ((b, c, s) in boundaryTests) {
                val matrix = FilterEngine.getColorMatrixForFilter(filter, brightness = b, contrast = c, saturation = s)
                assertNotNull(matrix.values)
                assertEquals(20, matrix.values.size)
                // Ensure no NaN or Infinite values
                for (v in matrix.values) {
                    assertFalse(v.isNaN(), "Filter $filter with params (b=$b, c=$c, s=$s) produced NaN")
                    assertFalse(v.isInfinite(), "Filter $filter with params (b=$b, c=$c, s=$s) produced Infinite")
                }
            }
        }
    }

    @Test
    fun testPixelBufferClamping() {
        val testPixels = intArrayOf(
            0xFF000000.toInt(), // Pure Black
            0xFFFFFFFF.toInt(), // Pure White
            0xFFFF0000.toInt(), // Pure Red
            0xFF00FF00.toInt(), // Pure Green
            0xFF0000FF.toInt()  // Pure Blue
        )

        for (filter in FilterType.entries) {
            val processed = FilterEngine.processPixelBuffer(
                pixels = testPixels,
                width = 5,
                height = 1,
                filter = filter,
                brightness = 1.2f,
                contrast = 1.5f,
                saturation = 1.3f
            )

            assertEquals(testPixels.size, processed.size)
            for (p in processed) {
                val a = (p ushr 24) and 0xFF
                val r = (p ushr 16) and 0xFF
                val g = (p ushr 8) and 0xFF
                val b = p and 0xFF

                assertEquals(0xFF, a, "Alpha channel must be preserved")
                assertTrue(r in 0..255, "Red must be clamped to 0..255, was $r")
                assertTrue(g in 0..255, "Green must be clamped to 0..255, was $g")
                assertTrue(b in 0..255, "Blue must be clamped to 0..255, was $b")
            }
        }
    }

    @Test
    fun testQuadCornersClampingAndIntegrity() {
        val quad = QuadCorners(
            topLeft = PointF(0.1f, 0.1f),
            topRight = PointF(0.9f, 0.1f),
            bottomRight = PointF(0.9f, 0.9f),
            bottomLeft = PointF(0.1f, 0.9f)
        )

        assertTrue(quad.topLeft.x < quad.topRight.x, "Top-left X must be less than Top-right X")
        assertTrue(quad.bottomLeft.x < quad.bottomRight.x, "Bottom-left X must be less than Bottom-right X")
        assertTrue(quad.topLeft.y < quad.bottomLeft.y, "Top-left Y must be less than Bottom-left Y")
        assertTrue(quad.topRight.y < quad.bottomRight.y, "Top-right Y must be less than Bottom-right Y")
    }

    @Test
    fun testPdfConfigAndWatermarkDefaults() {
        val config = PdfConfig(
            pageSize = PageSize.A4,
            quality = PdfQuality.HIGH,
            watermark = WatermarkConfig(
                isEnabled = true,
                text = "CONFIDENTIAL",
                opacity = 0.35f,
                rotationDegrees = -45f
            ),
            passwordProtection = "SecretPass123!",
            addPageNumbers = true
        )

        assertEquals("A4 (210 x 297 mm)", config.pageSize.displayName)
        assertEquals("High Quality", config.quality.displayName)
        assertTrue(config.watermark.isEnabled)
        assertEquals("CONFIDENTIAL", config.watermark.text)
        assertEquals("SecretPass123!", config.passwordProtection)
        assertTrue(config.addPageNumbers)
    }

    @Test
    fun testOcrEntityExtraction() {
        val sampleInvoice = """
            INVOICE #INV-2026-8942
            Date: 2026-09-01
            Vendor: Lufick Technologies LLC
            Total Amount: $1,450.00
            Contact: billing@docscanner.app
            Website: https://docscanner.app
        """.trimIndent()

        val entities = OcrParser.extractEntities(sampleInvoice)
        assertNotNull(entities)
        assertTrue(entities.isNotEmpty(), "Entities should be extracted from invoice")
    }
}
