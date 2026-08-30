package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import com.lufick.docscanner.engine.FilterEngine
import com.lufick.docscanner.engine.Homography
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class PlatformImageProcessor(private val context: Context) {

    actual suspend fun applyPerspectiveWarp(
        sourceImagePath: String,
        corners: QuadCorners,
        rotationDegrees: Int
    ): String = withContext(Dispatchers.IO) {
        val sourceFile = File(sourceImagePath)
        if (!sourceFile.exists()) return@withContext sourceImagePath // Fallback if missing
        
        val originalBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return@withContext sourceImagePath
        
        val width = originalBitmap.width.toFloat()
        val height = originalBitmap.height.toFloat()

        // Source points (the quadrilateral drawn by the user)
        val srcPoints = floatArrayOf(
            corners.topLeft.x * width, corners.topLeft.y * height,
            corners.topRight.x * width, corners.topRight.y * height,
            corners.bottomRight.x * width, corners.bottomRight.y * height,
            corners.bottomLeft.x * width, corners.bottomLeft.y * height
        )

        // Calculate destination dimensions (approximated based on corner distances)
        val topWidth = Math.hypot((srcPoints[2] - srcPoints[0]).toDouble(), (srcPoints[3] - srcPoints[1]).toDouble())
        val bottomWidth = Math.hypot((srcPoints[6] - srcPoints[4]).toDouble(), (srcPoints[7] - srcPoints[5]).toDouble())
        val destWidth = Math.max(topWidth, bottomWidth).toInt().coerceAtLeast(1)

        val leftHeight = Math.hypot((srcPoints[6] - srcPoints[0]).toDouble(), (srcPoints[7] - srcPoints[1]).toDouble())
        val rightHeight = Math.hypot((srcPoints[4] - srcPoints[2]).toDouble(), (srcPoints[5] - srcPoints[3]).toDouble())
        val destHeight = Math.max(leftHeight, rightHeight).toInt().coerceAtLeast(1)

        val dstPoints = floatArrayOf(
            0f, 0f,
            destWidth.toFloat(), 0f,
            destWidth.toFloat(), destHeight.toFloat(),
            0f, destHeight.toFloat()
        )

        val matrix = Matrix()
        matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

        // Output bitmap
        var resultBitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        // Draw the transformed image onto the canvas
        canvas.drawBitmap(originalBitmap, matrix, null)
        
        // Apply rotation if needed
        if (rotationDegrees != 0) {
            val rotMatrix = Matrix()
            rotMatrix.postRotate(rotationDegrees.toFloat())
            val rotated = Bitmap.createBitmap(resultBitmap, 0, 0, resultBitmap.width, resultBitmap.height, rotMatrix, true)
            resultBitmap.recycle()
            resultBitmap = rotated
        }

        originalBitmap.recycle()

        val outFile = File(context.filesDir, "warped_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { out ->
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        resultBitmap.recycle()
        outFile.absolutePath
    }

    actual suspend fun applyFilter(
        imagePath: String,
        filter: FilterType,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): String = withContext(Dispatchers.IO) {
        val sourceFile = File(imagePath)
        if (!sourceFile.exists()) return@withContext imagePath
        
        val originalBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return@withContext imagePath
        
        val outFile = File(context.filesDir, "filtered_${System.currentTimeMillis()}.jpg")
        val matrix = FilterEngine.getColorMatrixForFilter(filter, brightness, contrast, saturation).values
        val cm = ColorMatrix(matrix)
        val filterPaint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }

        val resultBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        // Draw the original bitmap with the filter paint
        canvas.drawBitmap(originalBitmap, 0f, 0f, filterPaint)

        FileOutputStream(outFile).use { out ->
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        
        originalBitmap.recycle()
        resultBitmap.recycle()
        
        outFile.absolutePath
    }

    
    actual suspend fun detectDocumentCorners(imagePath: String): QuadCorners = withContext(Dispatchers.IO) {
        val file = File(imagePath)
        if (!file.exists()) {
            return@withContext defaultDocumentQuad()
        }

        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            val origW = options.outWidth
            val origH = options.outHeight

            val sampleSize = Math.max(1, Math.max(origW, origH) / 360)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val sampled = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return@withContext defaultDocumentQuad()

            val sw = sampled.width
            val sh = sampled.height

            val gray = IntArray(sw * sh)
            val pixels = IntArray(sw * sh)
            sampled.getPixels(pixels, 0, sw, 0, 0, sw, sh)

            for (i in pixels.indices) {
                val p = pixels[i]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                gray[i] = (r * 299 + g * 587 + b * 114) / 1000
            }

            var maxColGrad = 0
            var bestLeft = (sw * 0.08f).toInt()
            for (x in (sw * 0.04f).toInt() until (sw * 0.40f).toInt()) {
                var grad = 0
                for (y in (sh * 0.10f).toInt() until (sh * 0.90f).toInt()) {
                    val idx = y * sw + x
                    grad += Math.abs(gray[idx + 1] - gray[idx - 1])
                }
                if (grad > maxColGrad) {
                    maxColGrad = grad
                    bestLeft = x
                }
            }

            maxColGrad = 0
            var bestRight = (sw * 0.92f).toInt()
            for (x in (sw * 0.60f).toInt() until (sw * 0.96f).toInt()) {
                var grad = 0
                for (y in (sh * 0.10f).toInt() until (sh * 0.90f).toInt()) {
                    val idx = y * sw + x
                    grad += Math.abs(gray[idx + 1] - gray[idx - 1])
                }
                if (grad > maxColGrad) {
                    maxColGrad = grad
                    bestRight = x
                }
            }

            var maxRowGrad = 0
            var bestTop = (sh * 0.08f).toInt()
            for (y in (sh * 0.04f).toInt() until (sh * 0.40f).toInt()) {
                var grad = 0
                for (x in (sw * 0.10f).toInt() until (sw * 0.90f).toInt()) {
                    val idx = y * sw + x
                    grad += Math.abs(gray[idx + sw] - gray[idx - sw])
                }
                if (grad > maxRowGrad) {
                    maxRowGrad = grad
                    bestTop = y
                }
            }

            maxRowGrad = 0
            var bestBottom = (sh * 0.92f).toInt()
            for (y in (sh * 0.60f).toInt() until (sh * 0.96f).toInt()) {
                var grad = 0
                for (x in (sw * 0.10f).toInt() until (sw * 0.90f).toInt()) {
                    val idx = y * sw + x
                    grad += Math.abs(gray[idx + sw] - gray[idx - sw])
                }
                if (grad > maxRowGrad) {
                    maxRowGrad = grad
                    bestBottom = y
                }
            }

            sampled.recycle()

            val minX = (bestLeft.toFloat() / sw).coerceIn(0.04f, 0.22f)
            val maxX = (bestRight.toFloat() / sw).coerceIn(0.78f, 0.96f)
            val minY = (bestTop.toFloat() / sh).coerceIn(0.04f, 0.22f)
            val maxY = (bestBottom.toFloat() / sh).coerceIn(0.78f, 0.96f)

            QuadCorners(
                topLeft = PointF(minX, minY),
                topRight = PointF(maxX, minY),
                bottomRight = PointF(maxX, maxY),
                bottomLeft = PointF(minX, maxY)
            )
        } catch (e: Exception) {
            defaultDocumentQuad()
        }
    }

    private fun defaultDocumentQuad() = QuadCorners(
        topLeft = PointF(0.08f, 0.08f),
        topRight = PointF(0.92f, 0.08f),
        bottomRight = PointF(0.92f, 0.92f),
        bottomLeft = PointF(0.08f, 0.92f)
    )

    actual suspend fun stitchIdCard(
        frontImagePath: String,
        backImagePath: String
    ): String = withContext(Dispatchers.IO) {
        val outFile = File(context.filesDir, "id_card_${System.currentTimeMillis()}.jpg")
        val a4Width = 1240
        val a4Height = 1754
        val resultBitmap = Bitmap.createBitmap(a4Width, a4Height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val frontBitmap = BitmapFactory.decodeFile(File(frontImagePath).absolutePath)
        val backBitmap = BitmapFactory.decodeFile(File(backImagePath).absolutePath)

        // Draw Front ID to top half
        if (frontBitmap != null) {
            val frontDest = android.graphics.Rect(120, 150, 1120, 750)
            canvas.drawBitmap(frontBitmap, null, frontDest, null)
            frontBitmap.recycle()
        }
        
        // Draw Back ID to bottom half
        if (backBitmap != null) {
            val backDest = android.graphics.Rect(120, 950, 1120, 1550)
            canvas.drawBitmap(backBitmap, null, backDest, null)
            backBitmap.recycle()
        }

        FileOutputStream(outFile).use { out ->
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        resultBitmap.recycle()
        outFile.absolutePath
    }
}

@Composable
actual fun rememberPlatformImageProcessor(): PlatformImageProcessor {
    val context = LocalContext.current
    return remember { PlatformImageProcessor(context) }
}
