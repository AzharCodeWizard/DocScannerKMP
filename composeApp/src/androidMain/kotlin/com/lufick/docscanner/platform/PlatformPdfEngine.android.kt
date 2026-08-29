package com.lufick.docscanner.platform

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
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

actual class PlatformPdfEngine(private val context: Context) {

    actual suspend fun generatePdf(
        documentTitle: String,
        pages: List<ScannedPage>,
        config: PdfConfig
    ): String = withContext(Dispatchers.IO) {
        val safeTitle = documentTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outFile = File(context.filesDir, "${safeTitle}_${currentTimeMillis()}.pdf")
        val pdfDoc = PdfDocument()

        val pageWidth = if (config.pageSize.widthPt > 0) config.pageSize.widthPt.toInt() else 595
        val pageHeight = if (config.pageSize.heightPt > 0) config.pageSize.heightPt.toInt() else 842

        pages.forEachIndexed { index, page ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val pdfPage = pdfDoc.startPage(pageInfo)
            val canvas = pdfPage.canvas

            canvas.drawColor(Color.WHITE)

            // Draw image if available
            val imgFile = File(page.processedImagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(page.processedImagePath)
                if (bitmap != null) {
                    val destRect = RectF(20f, 20f, pageWidth.toFloat() - 20f, pageHeight.toFloat() - 50f)
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
            if (config.watermark.isEnabled) {
                canvas.save()
                canvas.rotate(config.watermark.rotationDegrees, pageWidth / 2f, pageHeight / 2f)
                val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.LTGRAY
                    alpha = (config.watermark.opacity * 255).toInt()
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
                canvas.drawText("${index + 1} / ${pages.size}", pageWidth / 2f, pageHeight - 20f, footerPaint)
            }

            pdfDoc.finishPage(pdfPage)
        }

        FileOutputStream(outFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        outFile.absolutePath
    }

    actual suspend fun createPdf(
        imagePaths: List<String>,
        title: String,
        config: PdfConfig
    ): String = withContext(Dispatchers.IO) {
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outFile = File(context.filesDir, "${safeTitle}_${currentTimeMillis()}.pdf")
        val pdfDoc = PdfDocument()

        val pageWidth = if (config.pageSize.widthPt > 0) config.pageSize.widthPt.toInt() else 595
        val pageHeight = if (config.pageSize.heightPt > 0) config.pageSize.heightPt.toInt() else 842

        imagePaths.forEachIndexed { index, path ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val pdfPage = pdfDoc.startPage(pageInfo)
            val canvas = pdfPage.canvas

            canvas.drawColor(Color.WHITE)

            val imgFile = File(path)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(path)
                if (bitmap != null) {
                    val destRect = RectF(20f, 20f, pageWidth.toFloat() - 20f, pageHeight.toFloat() - 40f)
                    canvas.drawBitmap(bitmap, null, destRect, null)
                    bitmap.recycle()
                }
            } else {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 14f
                }
                canvas.drawText("$title - Page ${index + 1}", 40f, 60f, paint)
            }

            if (config.watermark.isEnabled) {
                canvas.save()
                canvas.rotate(config.watermark.rotationDegrees, pageWidth / 2f, pageHeight / 2f)
                val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.LTGRAY
                    alpha = (config.watermark.opacity * 255).toInt()
                    textSize = config.watermark.fontSizePt
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(config.watermark.text, pageWidth / 2f, pageHeight / 2f, wmPaint)
                canvas.restore()
            }

            pdfDoc.finishPage(pdfPage)
        }

        FileOutputStream(outFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        outFile.absolutePath
    }
}

@Composable
actual fun rememberPlatformPdfEngine(): PlatformPdfEngine {
    val context = LocalContext.current
    return remember(context) { PlatformPdfEngine(context) }
}
