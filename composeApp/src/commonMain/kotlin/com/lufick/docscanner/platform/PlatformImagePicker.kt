package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable

interface PlatformImagePicker {
    fun launchImagePicker(onImagePicked: (imagePath: String) -> Unit)
}

@Composable
expect fun rememberPlatformImagePicker(): PlatformImagePicker
