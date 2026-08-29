import re

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'r') as f:
    content = f.read()

filter_old = """    actual suspend fun applyFilter(
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
    }"""

filter_new = """    actual suspend fun applyFilter(
        imagePath: String,
        filter: FilterType,
        brightness: Float,
        contrast: Float
    ): String = withContext(Dispatchers.IO) {
        val sourceFile = File(imagePath)
        if (!sourceFile.exists()) return@withContext imagePath
        
        val originalBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return@withContext imagePath
        
        val outFile = File(context.filesDir, "filtered_${System.currentTimeMillis()}.jpg")
        val matrix = FilterEngine.getColorMatrixForFilter(filter, brightness, contrast).values
        val cm = ColorMatrix(matrix)
        val filterPaint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }

        val resultBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        // Draw the original bitmap with the filter paint
        canvas.drawBitmap(originalBitmap, 0f, 0f, filterPaint)

        FileOutputStream(outFile).use { out ->
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        originalBitmap.recycle()
        resultBitmap.recycle()
        
        outFile.absolutePath
    }"""

content = content.replace(filter_old, filter_new)

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'w') as f:
    f.write(content)
