package com.lufick.docscanner.platform

import com.lufick.docscanner.engine.OcrParser
import com.lufick.docscanner.model.OcrBlock
import com.lufick.docscanner.model.OcrBoundingBox
import com.lufick.docscanner.model.OcrLine
import com.lufick.docscanner.model.OcrResult

actual class PlatformOcrEngine {

    actual suspend fun recognizeText(imagePath: String): OcrResult {
        val sampleText = """WHOLE FOODS MARKET
Date: Oct 25, 2026   Inv #84920
1x Organic Oat Milk         $4.99
2x Hass Avocados            $3.50
1x Sourdough Artisan Bread  $5.25
1x Ceremonial Matcha Tea   $12.99
---------------------------------
TOTAL DUE                  $26.73"""

        val entities = OcrParser.extractEntities(sampleText)

        return OcrResult(
            fullText = sampleText,
            blocks = listOf(
                OcrBlock(
                    text = sampleText,
                    lines = sampleText.lines().map {
                        OcrLine(it, OcrBoundingBox(0.1f, 0.1f, 0.9f, 0.2f))
                    },
                    boundingBox = OcrBoundingBox(0.1f, 0.1f, 0.9f, 0.9f)
                )
            ),
            entities = entities,
            detectedLanguage = "en",
            processingTimeMs = 120L
        )
    }
}
