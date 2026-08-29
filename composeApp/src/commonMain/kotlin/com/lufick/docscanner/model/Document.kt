package com.lufick.docscanner.model

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val id: String,
    val title: String,
    val folderId: String? = null,
    val tags: List<String> = emptyList(),
    val pages: List<ScannedPage> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isFavorite: Boolean = false,
    val isEncrypted: Boolean = false,
    val passwordHash: String? = null,
    val notes: String = "",
    val totalSizeBytes: Long = 0L
) {
    val pageCount: Int get() = pages.size
    val firstThumbnail: String? get() = pages.firstOrNull()?.processedImagePath ?: pages.firstOrNull()?.originalImagePath
}
