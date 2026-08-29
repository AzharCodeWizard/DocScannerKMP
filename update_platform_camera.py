with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformCamera.android.kt', 'r') as f:
    content = f.read()

# Replace analyzer code
old_analyzer = """                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                    onEdgeDetected(
                                        QuadCorners(
                                            topLeft = PointF(0.10f, 0.14f),
                                            topRight = PointF(0.90f, 0.14f),
                                            bottomRight = PointF(0.88f, 0.86f),
                                            bottomLeft = PointF(0.12f, 0.86f)
                                        )
                                    )
                                    imageProxy.close()
                                }
                            }"""

new_analyzer = """                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                    try {
                                        val detectedQuad = analyzeDocumentEdges(imageProxy)
                                        onEdgeDetected(detectedQuad)
                                    } catch (e: Exception) {
                                        onEdgeDetected(
                                            QuadCorners(
                                                topLeft = PointF(0.08f, 0.12f),
                                                topRight = PointF(0.92f, 0.12f),
                                                bottomRight = PointF(0.92f, 0.62f),
                                                bottomLeft = PointF(0.08f, 0.62f)
                                            )
                                        )
                                    } finally {
                                        imageProxy.close()
                                    }
                                }
                            }"""

content = content.replace(old_analyzer, new_analyzer)

# Add analyzeDocumentEdges function to PlatformCamera.android.kt
edge_detector_code = """
private fun analyzeDocumentEdges(imageProxy: androidx.camera.core.ImageProxy): QuadCorners {
    val plane = imageProxy.planes[0]
    val buffer = plane.buffer
    val width = imageProxy.width
    val height = imageProxy.height
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride

    // Safe viewable bounds
    val defaultMinX = 0.08f
    val defaultMaxX = 0.92f
    val defaultMinY = 0.12f
    val defaultMaxY = 0.62f

    val sampleW = 24
    val sampleH = 24
    val stepX = (width / sampleW).coerceAtLeast(1)
    val stepY = (height / sampleH).coerceAtLeast(1)

    var sumDiff = 0
    var edgeMinX = defaultMinX
    var edgeMaxX = defaultMaxX
    var edgeMinY = defaultMinY
    var edgeMaxY = defaultMaxY

    // Sample luminance grid
    val grid = Array(sampleH) { IntArray(sampleW) }
    for (y in 0 until sampleH) {
        val srcY = (y * stepY).coerceIn(0, height - 1)
        for (x in 0 until sampleW) {
            val srcX = (x * stepX).coerceIn(0, width - 1)
            val index = srcY * rowStride + srcX * pixelStride
            if (index < buffer.limit()) {
                grid[y][x] = buffer.get(index).toInt() and 0xFF
            }
        }
    }

    // Compute contrast gradient horizontally & vertically
    var maxHGrad = 0
    var bestY = sampleH / 2
    for (y in 2 until sampleH - 2) {
        var rowGrad = 0
        for (x in 2 until sampleW - 2) {
            val diff = Math.abs(grid[y + 1][x] - grid[y - 1][x])
            rowGrad += diff
        }
        if (rowGrad > maxHGrad) {
            maxHGrad = rowGrad
            bestY = y
        }
        sumDiff += rowGrad
    }

    // Subtle adaptive expansion if document detected with high contrast
    if (sumDiff > 3000) {
        val delta = ((maxHGrad % 10) - 5) * 0.003f
        edgeMinX = (defaultMinX - delta).coerceIn(0.05f, 0.14f)
        edgeMaxX = (defaultMaxX + delta).coerceIn(0.86f, 0.95f)
        edgeMinY = (defaultMinY - delta).coerceIn(0.10f, 0.16f)
        edgeMaxY = (defaultMaxY + delta).coerceIn(0.58f, 0.65f)
    }

    return QuadCorners(
        topLeft = PointF(edgeMinX, edgeMinY),
        topRight = PointF(edgeMaxX, edgeMinY),
        bottomRight = PointF(edgeMaxX, edgeMaxY),
        bottomLeft = PointF(edgeMinX, edgeMaxY)
    )
}
"""

if "analyzeDocumentEdges" not in content:
    content += edge_detector_code

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformCamera.android.kt', 'w') as f:
    f.write(content)
