package com.lufick.docscanner.platform

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File

@Composable
actual fun LocalImage(
    path: String,
    modifier: Modifier,
    colorFilter: ColorFilter?,
    rotationDegrees: Int
) {
    val bitmap = remember(path) {
        val file = File(path)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } else {
            null
        }
    }
    
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = if (rotationDegrees != 0) modifier.rotate(rotationDegrees.toFloat()) else modifier,
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter
        )
    }
}

