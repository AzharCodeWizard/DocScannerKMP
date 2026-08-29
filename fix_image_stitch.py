import re

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'r') as f:
    content = f.read()

stitch_old = """    actual suspend fun stitchIdCard(
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
    }"""

stitch_new = """    actual suspend fun stitchIdCard(
        frontImagePath: String,
        backImagePath: String
    ): String = withContext(Dispatchers.IO) {
        val outFile = File(context.filesDir, "id_card_${System.currentTimeMillis()}.jpg")
        val a4Width = 1240
        val a4Height = 1754
        val resultBitmap = Bitmap.createBitmap(a4Width, a4Height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val frontBitmap = BitmapFactory.decodeFile(File(frontImagePath).absolutePath)
        val backBitmap = BitmapFactory.decodeFile(File(backImagePath).absolutePath)

        // Draw Front ID to top half
        if (frontBitmap != null) {
            val frontDest = android.graphics.Rect(120, 150, 1120, 750)
            canvas.drawBitmap(frontBitmap, null, frontDest, null)
            frontBitmap.recycle()
        }
        
        // Draw Back ID to bottom half
        if (backBitmap != null) {
            val backDest = android.graphics.Rect(120, 950, 1120, 1550)
            canvas.drawBitmap(backBitmap, null, backDest, null)
            backBitmap.recycle()
        }

        FileOutputStream(outFile).use { out ->
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        resultBitmap.recycle()
        outFile.absolutePath
    }"""

content = content.replace(stitch_old, stitch_new)

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'w') as f:
    f.write(content)
