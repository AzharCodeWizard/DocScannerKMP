package com.lufick.docscanner.engine

import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 4-point Projective Transformation & Homography Matrix Calculator.
 * Maps arbitrary quadrilateral corners to an upright rectangular frame.
 */
object Homography {

    data class Matrix3x3(val data: FloatArray) {
        init {
            require(data.size == 9) { "Matrix3x3 must have 9 elements" }
        }

        operator fun get(row: Int, col: Int): Float = data[row * 3 + col]

        fun transformPoint(p: PointF): PointF {
            val px = p.x
            val py = p.y
            val w = data[6] * px + data[7] * py + data[8]
            val invW = if (abs(w) > 1e-7f) 1.0f / w else 1.0f
            val xPrime = (data[0] * px + data[1] * py + data[2]) * invW
            val yPrime = (data[3] * px + data[4] * py + data[5]) * invW
            return PointF(xPrime, yPrime)
        }
    }

    /**
     * Compute 3x3 Homography Matrix from 4 source points to 4 destination points.
     */
    fun computeHomography(src: QuadCorners, dstWidth: Float, dstHeight: Float): Matrix3x3 {
        val srcPts = arrayOf(src.topLeft, src.topRight, src.bottomRight, src.bottomLeft)
        val dstPts = arrayOf(
            PointF(0f, 0f),
            PointF(dstWidth, 0f),
            PointF(dstWidth, dstHeight),
            PointF(0f, dstHeight)
        )

        // 8 Linear equations: A * h = b (Direct Linear Transformation with h33 = 1)
        val a = Array(8) { FloatArray(8) }
        val b = FloatArray(8)

        for (i in 0 until 4) {
            val sx = srcPts[i].x
            val sy = srcPts[i].y
            val dx = dstPts[i].x
            val dy = dstPts[i].y

            val r1 = i * 2
            val r2 = i * 2 + 1

            a[r1][0] = sx
            a[r1][1] = sy
            a[r1][2] = 1f
            a[r1][3] = 0f
            a[r1][4] = 0f
            a[r1][5] = 0f
            a[r1][6] = -sx * dx
            a[r1][7] = -sy * dx
            b[r1] = dx

            a[r2][0] = 0f
            a[r2][1] = 0f
            a[r2][2] = 0f
            a[r2][3] = sx
            a[r2][4] = sy
            a[r2][5] = 1f
            a[r2][6] = -sx * dy
            a[r2][7] = -sy * dy
            b[r2] = dy
        }

        val h = solveGaussian(a, b)
        return Matrix3x3(
            floatArrayOf(
                h[0], h[1], h[2],
                h[3], h[4], h[5],
                h[6], h[7], 1.0f
            )
        )
    }

    /**
     * Calculate optimal unwarped output dimensions based on quad geometry.
     */
    fun calculateUnwarpedDimensions(quad: QuadCorners, sourceImgWidth: Int, sourceImgHeight: Int): Pair<Int, Int> {
        val tl = PointF(quad.topLeft.x * sourceImgWidth, quad.topLeft.y * sourceImgHeight)
        val tr = PointF(quad.topRight.x * sourceImgWidth, quad.topRight.y * sourceImgHeight)
        val br = PointF(quad.bottomRight.x * sourceImgWidth, quad.bottomRight.y * sourceImgHeight)
        val bl = PointF(quad.bottomLeft.x * sourceImgWidth, quad.bottomLeft.y * sourceImgHeight)

        val topWidth = hypot(tr.x - tl.x, tr.y - tl.y)
        val bottomWidth = hypot(br.x - bl.x, br.y - bl.y)
        val maxWidth = max(topWidth, bottomWidth).roundToInt()

        val leftHeight = hypot(bl.x - tl.x, bl.y - tl.y)
        val rightHeight = hypot(br.x - tr.x, br.y - tr.y)
        val maxHeight = max(leftHeight, rightHeight).roundToInt()

        return Pair(max(100, maxWidth), max(100, maxHeight))
    }

    private fun solveGaussian(a: Array<FloatArray>, b: FloatArray): FloatArray {
        val n = 8
        val aug = Array(n) { i -> FloatArray(n + 1) { j -> if (j < n) a[i][j] else b[i] } }

        for (p in 0 until n) {
            var maxRow = p
            for (i in p + 1 until n) {
                if (abs(aug[i][p]) > abs(aug[maxRow][p])) {
                    maxRow = i
                }
            }
            val temp = aug[p]
            aug[p] = aug[maxRow]
            aug[maxRow] = temp

            val pivot = aug[p][p]
            if (abs(pivot) < 1e-9f) continue

            for (i in p + 1 until n) {
                val factor = aug[i][p] / pivot
                for (j in p until n + 1) {
                    aug[i][j] -= factor * aug[p][j]
                }
            }
        }

        val x = FloatArray(n)
        for (i in n - 1 downTo 0) {
            var sum = aug[i][n]
            for (j in i + 1 until n) {
                sum -= aug[i][j] * x[j]
            }
            x[i] = if (abs(aug[i][i]) > 1e-9f) sum / aug[i][i] else 0f
        }
        return x
    }
}
