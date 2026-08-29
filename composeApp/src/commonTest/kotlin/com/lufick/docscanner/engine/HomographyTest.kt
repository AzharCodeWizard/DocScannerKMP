package com.lufick.docscanner.engine

import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomographyTest {

    @Test
    fun testIdentityMapping() {
        val quad = QuadCorners(
            topLeft = PointF(0f, 0f),
            topRight = PointF(100f, 0f),
            bottomRight = PointF(100f, 100f),
            bottomLeft = PointF(0f, 100f)
        )
        val matrix = Homography.computeHomography(quad, 100f, 100f)
        val transformed = matrix.transformPoint(PointF(50f, 50f))

        assertTrue(kotlin.math.abs(transformed.x - 50f) < 1.0f)
        assertTrue(kotlin.math.abs(transformed.y - 50f) < 1.0f)
    }

    @Test
    fun testDimensionsCalculation() {
        val quad = QuadCorners(
            topLeft = PointF(0.1f, 0.1f),
            topRight = PointF(0.9f, 0.1f),
            bottomRight = PointF(0.9f, 0.9f),
            bottomLeft = PointF(0.1f, 0.9f)
        )
        val (width, height) = Homography.calculateUnwarpedDimensions(quad, 1000, 1000)
        assertEquals(800, width)
        assertEquals(800, height)
    }
}
