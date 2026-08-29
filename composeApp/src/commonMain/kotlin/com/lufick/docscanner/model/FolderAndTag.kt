package com.lufick.docscanner.model

import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val id: String,
    val name: String,
    val iconName: String = "folder",
    val colorHex: String = "#10B981",
    val documentCount: Int = 0,
    val createdAt: Long = 0L
)

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val colorHex: String = "#3B82F6"
)
