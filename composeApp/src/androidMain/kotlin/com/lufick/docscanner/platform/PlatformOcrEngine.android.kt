package com.lufick.docscanner.platform

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.lufick.docscanner.engine.OcrParser
import com.lufick.docscanner.model.OcrBlock
import com.lufick.docscanner.model.OcrBoundingBox
import com.lufick.docscanner.model.OcrLine
import com.lufick.docscanner.model.OcrResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

actual class PlatformOcrEngine(private val context: Context) {

    private val recognizer = TextRecognition.getClient()

    actual suspend fun recognizeText(imagePath: String): OcrResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val file = File(imagePath)
        
        if (!file.exists()) {
            return@withContext OcrResult(
                fullText = "",
                blocks = emptyList(),
                entities = emptyList(),
                detectedLanguage = "en",
                processingTimeMs = 0L
            )
        }

        try {
            val inputImage = InputImage.fromFilePath(context, Uri.fromFile(file))
            val visionText = suspendCancellableCoroutine { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { text ->
                        continuation.resume(text)
                    }
                    .addOnFailureListener { exc ->
                        continuation.resume(null)
                    }
            }

            if (visionText == null || visionText.text.isBlank()) {
                return@withContext OcrResult(
                    fullText = "No readable text found in document.",
                    blocks = emptyList(),
                    entities = emptyList(),
                    detectedLanguage = "en",
                    processingTimeMs = System.currentTimeMillis() - start
                )
            }

            val imgW = if (inputImage.width > 0) inputImage.width.toFloat() else 1f
            val imgH = if (inputImage.height > 0) inputImage.height.toFloat() else 1f

            val blocks = visionText.textBlocks.map { block ->
                val bBox = block.boundingBox
                val normBBox = if (bBox != null) {
                    OcrBoundingBox(
                        bBox.left / imgW,
                        bBox.top / imgH,
                        bBox.right / imgW,
                        bBox.bottom / imgH
                    )
                } else {
                    OcrBoundingBox(0f, 0f, 1f, 1f)
                }

                val lines = block.lines.map { line ->
                    val lBox = line.boundingBox
                    val normLBox = if (lBox != null) {
                        OcrBoundingBox(
                            lBox.left / imgW,
                            lBox.top / imgH,
                            lBox.right / imgW,
                            lBox.bottom / imgH
                        )
                    } else {
                        OcrBoundingBox(0f, 0f, 1f, 1f)
                    }
                    OcrLine(text = line.text, boundingBox = normLBox)
                }

                OcrBlock(text = block.text, lines = lines, boundingBox = normBBox)
            }

            val fullText = visionText.text
            val entities = OcrParser.extractEntities(fullText)
            val elapsed = System.currentTimeMillis() - start

            OcrResult(
                fullText = fullText,
                blocks = blocks,
                entities = entities,
                detectedLanguage = "en",
                processingTimeMs = elapsed
            )
        } catch (e: Exception) {
            OcrResult(
                fullText = "OCR Recognition Error: ${e.message}",
                blocks = emptyList(),
                entities = emptyList(),
                detectedLanguage = "en",
                processingTimeMs = System.currentTimeMillis() - start
            )
        }
    }
}

@androidx.compose.runtime.Composable
actual fun rememberPlatformOcrEngine(): PlatformOcrEngine {
    val context = androidx.compose.ui.platform.LocalContext.current
    return androidx.compose.runtime.remember(context) { PlatformOcrEngine(context) }
}


