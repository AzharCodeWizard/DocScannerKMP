package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable

expect class PlatformShare {
    fun shareFile(filePath: String, mimeType: String = "application/pdf")
    fun shareText(text: String)
    fun printDocument(filePath: String)
}

@Composable
expect fun rememberPlatformShare(): PlatformShare
