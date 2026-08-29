import re

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'r') as f:
    content = f.read()

# Make sure BitmapFactory is imported
if 'import android.graphics.BitmapFactory' not in content:
    content = content.replace('import android.graphics.Bitmap', 'import android.graphics.Bitmap\nimport android.graphics.BitmapFactory')

warp_old = """    actual suspend fun applyPerspectiveWarp(
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
    }"""

warp_new = """    actual suspend fun applyPerspectiveWarp(
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
    }"""

content = content.replace(warp_old, warp_new)

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'w') as f:
    f.write(content)
