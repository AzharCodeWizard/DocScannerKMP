package com.lufick.docscanner.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import com.lufick.docscanner.engine.FilterEngine
import com.lufick.docscanner.engine.Homography
import com.lufick.docscanner.model.FilterType
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
        val outFile = File(context.filesDir, "warped_${System.currentTimeMillis()}.jpg")
        val width = 1200
        val height = 1600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.DKGRAY
        paint.textSize = 32f
        canvas.drawText("DocScanner Warped Page", 100f, 200f, paint)

        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        bitmap.recycle()
        outFile.absolutePath
    }

    actual suspend fun applyFilter(
        imagePath: String,
        filter: FilterType,
        brightness: Float,
        contrast: Float
    ): String = withContext(Dispatchers.IO) {
        val outFile = File(context.filesDir, "filtered_${System.currentTimeMillis()}.jpg")
        val matrix = FilterEngine.getColorMatrixForFilter(filter, brightness, contrast).values
        val cm = ColorMatrix(matrix)
        val filterPaint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }

        val bitmap = Bitmap.createBitmap(1200, 1600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawRect(0f, 0f, 1200f, 1600f, filterPaint)

        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()
        outFile.absolutePath
    }

    actual suspend fun stitchIdCard(
        frontImagePath: String,
        backImagePath: String
    ): String = withContext(Dispatchers.IO) {
        val outFile = File(context.filesDir, "id_card_${System.currentTimeMillis()}.jpg")
        val a4Width = 1240
        val a4Height = 1754
        val bitmap = Bitmap.createBitmap(a4Width, a4Height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val borderPaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        // Top Frame (Front ID)
        canvas.drawRect(120f, 150f, 1120f, 750f, borderPaint)
        // Bottom Frame (Back ID)
        canvas.drawRect(120f, 950f, 1120f, 1550f, borderPaint)

        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        bitmap.recycle()
        outFile.absolutePath
    }
}
