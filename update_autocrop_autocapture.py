# 1. Update PlatformImageProcessor.kt
with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.kt', 'r') as f:
    content = f.read()

if "detectDocumentCorners" not in content:
    content = content.replace("suspend fun stitchIdCard(", "suspend fun detectDocumentCorners(imagePath: String): QuadCorners\n\n    suspend fun stitchIdCard(")
    with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.kt', 'w') as f:
        f.write(content)

# 2. Update PlatformImageProcessor.ios.kt
with open('composeApp/src/iosMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.ios.kt', 'r') as f:
    ios_content = f.read()

if "detectDocumentCorners" not in ios_content:
    ios_content = ios_content.replace("actual suspend fun stitchIdCard(", """actual suspend fun detectDocumentCorners(imagePath: String): QuadCorners {
        return QuadCorners(
            topLeft = PointF(0.08f, 0.08f),
            topRight = PointF(0.92f, 0.08f),
            bottomRight = PointF(0.92f, 0.92f),
            bottomLeft = PointF(0.08f, 0.92f)
        )
    }

    actual suspend fun stitchIdCard(""")
    with open('composeApp/src/iosMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.ios.kt', 'w') as f:
        f.write(ios_content)

# 3. Update PlatformImageProcessor.android.kt
with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'r') as f:
    android_content = f.read()

detect_corners_android = """
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
"""

if "detectDocumentCorners" not in android_content:
    android_content = android_content.replace("actual suspend fun stitchIdCard(", detect_corners_android + "\n    actual suspend fun stitchIdCard(")
    with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'w') as f:
        f.write(android_content)

