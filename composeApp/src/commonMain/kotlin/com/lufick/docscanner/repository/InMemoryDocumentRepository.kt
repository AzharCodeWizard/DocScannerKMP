package com.lufick.docscanner.repository

import com.lufick.docscanner.util.currentTimeMillis
import com.lufick.docscanner.model.Document
import com.lufick.docscanner.model.Folder
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryDocumentRepository : DocumentRepository {

    private val defaultFolders = listOf(
        Folder("f_all", "All Docs", "folder", "#10B981", 0, currentTimeMillis()),
        Folder("f_receipts", "Receipts", "receipt", "#3B82F6", 0, currentTimeMillis()),
        Folder("f_id", "ID Cards", "badge", "#F59E0B", 0, currentTimeMillis()),
        Folder("f_contracts", "Contracts", "gavel", "#8B5CF6", 0, currentTimeMillis())
    )

    private val defaultTags = listOf(
        Tag("t_receipt", "Receipt", "#3B82F6"),
        Tag("t_ocr", "OCR Ready", "#10B981"),
        Tag("t_id", "Identity", "#EC4899"),
        Tag("t_contract", "Contract", "#8B5CF6")
    )

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    private val _folders = MutableStateFlow<List<Folder>>(defaultFolders)
    private val _tags = MutableStateFlow<List<Tag>>(defaultTags)

    override fun getAllDocuments(): Flow<List<Document>> = _documents.asStateFlow()

    override fun getDocumentById(id: String): Flow<Document?> =
        _documents.map { list -> list.find { it.id == id } }

    override fun getFolders(): Flow<List<Folder>> = _folders.asStateFlow()

    override fun getTags(): Flow<List<Tag>> = _tags.asStateFlow()

    override suspend fun saveDocument(doc: Document) {
        val current = _documents.value.toMutableList()
        val index = current.indexOfFirst { it.id == doc.id }
        if (index >= 0) {
            current[index] = doc
        } else {
            current.add(0, doc)
        }
        _documents.value = current
    }

    override suspend fun deleteDocument(id: String) {
        _documents.value = _documents.value.filterNot { it.id == id }
    }

    override suspend fun toggleFavorite(id: String) {
        val current = _documents.value.map { doc ->
            if (doc.id == id) doc.copy(isFavorite = !doc.isFavorite) else doc
        }
        _documents.value = current
    }

    override suspend fun updateDocumentTitle(id: String, newTitle: String) {
        val current = _documents.value.map { doc ->
            if (doc.id == id) doc.copy(title = newTitle, updatedAt = currentTimeMillis()) else doc
        }
        _documents.value = current
    }

    override suspend fun addPageToDocument(documentId: String, page: ScannedPage) {
        val current = _documents.value.map { doc ->
            if (doc.id == documentId) {
                val updatedPages = doc.pages + page.copy(pageNumber = doc.pages.size + 1)
                doc.copy(pages = updatedPages, updatedAt = currentTimeMillis())
            } else doc
        }
        _documents.value = current
    }

    override suspend fun updatePage(documentId: String, page: ScannedPage) {
        val current = _documents.value.map { doc ->
            if (doc.id == documentId) {
                val updatedPages = doc.pages.map { if (it.id == page.id) page else it }
                doc.copy(pages = updatedPages, updatedAt = currentTimeMillis())
            } else doc
        }
        _documents.value = current
    }

    override suspend fun reorderPages(documentId: String, newPages: List<ScannedPage>) {
        val indexed = newPages.mapIndexed { idx, p -> p.copy(pageNumber = idx + 1) }
        val current = _documents.value.map { doc ->
            if (doc.id == documentId) doc.copy(pages = indexed, updatedAt = currentTimeMillis()) else doc
        }
        _documents.value = current
    }

    override suspend fun deletePage(documentId: String, pageId: String) {
        val current = _documents.value.map { doc ->
            if (doc.id == documentId) {
                val filtered = doc.pages.filterNot { it.id == pageId }
                    .mapIndexed { idx, p -> p.copy(pageNumber = idx + 1) }
                doc.copy(pages = filtered, updatedAt = currentTimeMillis())
            } else doc
        }
        _documents.value = current
    }

    override suspend fun createFolder(name: String, colorHex: String) {
        val newFolder = Folder(
            id = "f_" + currentTimeMillis(),
            name = name,
            colorHex = colorHex,
            documentCount = 0,
            createdAt = currentTimeMillis()
        )
        _folders.value = _folders.value + newFolder
    }

    override suspend fun deleteFolder(id: String) {
        _folders.value = _folders.value.filterNot { it.id == id }
    }

    override suspend fun createTag(name: String, colorHex: String) {
        val newTag = Tag(
            id = "t_" + currentTimeMillis(),
            name = name,
            colorHex = colorHex
        )
        _tags.value = _tags.value + newTag
    }
}
