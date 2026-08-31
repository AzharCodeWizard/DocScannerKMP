package com.lufick.docscanner.platform

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

private const val TAG = "DocScannerPicker"

@Composable
actual fun rememberPlatformImagePicker(): PlatformImagePicker {
    val context = LocalContext.current
    var callbackHolder = remember { mutableMapOf<String, (String) -> Unit>() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val destinationPath = copyUriToLocalCache(context, uri)
            if (destinationPath != null) {
                callbackHolder["cb"]?.invoke(destinationPath)
            }
        }
    }

    return remember(context, launcher) {
        object : PlatformImagePicker {
            override fun launchImagePicker(onImagePicked: (imagePath: String) -> Unit) {
                callbackHolder["cb"] = onImagePicked
                launcher.launch("image/*")
            }
        }
    }
}

private fun copyUriToLocalCache(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val cacheFile = File(context.cacheDir, "imported_scan_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(cacheFile)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        cacheFile.absolutePath
    } catch (e: Exception) {
        Log.e(TAG, "Failed to copy imported image", e)
        null
    }
}
