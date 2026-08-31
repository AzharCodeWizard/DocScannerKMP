package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPlatformImagePicker(): PlatformImagePicker {
    return remember {
        object : PlatformImagePicker {
            override fun launchImagePicker(onImagePicked: (imagePath: String) -> Unit) {
                onImagePicked("ios_imported_sample.jpg")
            }
        }
    }
}
