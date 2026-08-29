package com.lufick.docscanner.platform

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.ScannedPage
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
        val outFile = File(context.filesDir, "${safeTitle}_${System.currentTimeMillis()}.pdf")
        val pdfDoc = PdfDocument()

        val pageWidth = if (config.pageSize.widthPt > 0) config.pageSize.widthPt.toInt() else 595
        val pageHeight = if (config.pageSize.heightPt > 0) config.pageSize.heightPt.toInt() else 842

        pages.forEachIndexed { index, page ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val pdfPage = pdfDoc.startPage(pageInfo)
            val canvas = pdfPage.canvas

            // Background
            canvas.drawColor(Color.WHITE)

            // Header & Content simulation
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
                canvas.drawText("${index + 1} / ${pages.size}", pageWidth / 2f, pageHeight - 30f, footerPaint)
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
