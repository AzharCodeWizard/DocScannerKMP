package com.lufick.docscanner.platform

expect class PlatformShare {
    fun shareFile(filePath: String, mimeType: String = "application/pdf")
    fun shareText(text: String)
    fun printDocument(filePath: String)
}
