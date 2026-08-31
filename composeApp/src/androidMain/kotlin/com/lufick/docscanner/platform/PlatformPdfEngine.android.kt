package com.lufick.docscanner.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.util.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

actual class PlatformPdfEngine(private val context: Context) {

    actual suspend fun generatePdf(
        documentTitle: String,
        pages: List<ScannedPage>,
        config: PdfConfig
    ): String = withContext(Dispatchers.IO) {
        val safeTitle = documentTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outFile = File(context.filesDir, "${safeTitle}_${currentTimeMillis()}.pdf")
        val pdfDoc = PdfDocument()

        try {
            val pageWidth = if (config.pageSize.widthPt > 0) config.pageSize.widthPt.toInt() else 595
            val pageHeight = if (config.pageSize.heightPt > 0) config.pageSize.heightPt.toInt() else 842

            pages.forEachIndexed { index, page ->
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val pdfPage = pdfDoc.startPage(pageInfo)
                val canvas = pdfPage.canvas

                canvas.drawColor(Color.WHITE)

                val imagePath = page.processedImagePath.ifBlank { page.originalImagePath }
                val imgFile = File(imagePath)

                if (imgFile.exists()) {
                    var bitmap = BitmapFactory.decodeFile(imagePath)
                    if (bitmap != null) {
                        // Apply page rotation if needed
                        if (page.rotationDegrees != 0) {
                            val matrix = Matrix().apply { postRotate(page.rotationDegrees.toFloat()) }
                            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                            if (rotated != bitmap) {
                                bitmap.recycle()
                                bitmap = rotated
                            }
                        }

                        // Maintain aspect ratio with margins
                        val marginHorizontal = 20f
                        val marginTop = 20f
                        val marginBottom = if (config.addPageNumbers) 36f else 20f
                        val availW = pageWidth - marginHorizontal * 2
                        val availH = pageHeight - marginTop - marginBottom

                        val scale = min(availW / bitmap.width, availH / bitmap.height)
                        val drawW = bitmap.width * scale
                        val drawH = bitmap.height * scale
                        val left = marginHorizontal + (availW - drawW) / 2f
                        val top = marginTop + (availH - drawH) / 2f

                        val destRect = RectF(left, top, left + drawW, top + drawH)
                        canvas.drawBitmap(bitmap, null, destRect, null)
                        bitmap.recycle()
                    }
                } else {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.BLACK
                        textSize = 14f
                    }
                    canvas.drawText("$documentTitle - Page ${index + 1}", 40f, 60f, paint)

                    if (!page.ocrText.isNullOrEmpty()) {
                        val lines = page.ocrText.lines()
                        var y = 100f
                        val ocrPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.DKGRAY
                            textSize = 10f
                        }
                        for (line in lines) {
                            canvas.drawText(line, 40f, y, ocrPaint)
                            y += 16f
                            if (y > pageHeight - 80) break
                        }
                    }
                }

                // Watermark
                if (config.watermark.isEnabled && config.watermark.text.isNotBlank()) {
                    canvas.save()
                    canvas.rotate(config.watermark.rotationDegrees, pageWidth / 2f, pageHeight / 2f)
                    val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.LTGRAY
                        alpha = (config.watermark.opacity.coerceIn(0f, 1f) * 255).toInt()
                        textSize = config.watermark.fontSizePt
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(config.watermark.text, pageWidth / 2f, pageHeight / 2f, wmPaint)
                    canvas.restore()
                }

                // Page numbers
                if (config.addPageNumbers) {
                    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.GRAY
                        textSize = 10f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("${index + 1} / ${pages.size}", pageWidth / 2f, pageHeight - 16f, footerPaint)
                }

                pdfDoc.finishPage(pdfPage)
            }

            FileOutputStream(outFile).use { out ->
                pdfDoc.writeTo(out)
            }
            outFile.absolutePath
        } finally {
            try {
                pdfDoc.close()
            } catch (ignored: Exception) {}
        }
    }

    actual suspend fun createPdf(
        imagePaths: List<String>,
        title: String,
        config: PdfConfig
    ): String = withContext(Dispatchers.IO) {
        val pages = imagePaths.mapIndexed { idx, path ->
            ScannedPage(
                id = "p_$idx",
                pageNumber = idx + 1,
                originalImagePath = path,
                processedImagePath = path
            )
        }
        generatePdf(title, pages, config)
    }
}

@Composable
actual fun rememberPlatformPdfEngine(): PlatformPdfEngine {
    val context = LocalContext.current
    return remember(context) { PlatformPdfEngine(context) }
}
