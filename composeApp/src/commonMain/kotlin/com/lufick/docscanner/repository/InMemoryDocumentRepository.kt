package com.lufick.docscanner.repository

import com.lufick.docscanner.util.currentTimeMillis

import com.lufick.docscanner.model.Document
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.Folder
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryDocumentRepository : DocumentRepository {

    private val sampleFolders = listOf(
        Folder("f_all", "All Docs", "folder", "#10B981", 4, 1729000000000L),
        Folder("f_receipts", "Receipts", "receipt", "#3B82F6", 2, 1729000000000L),
        Folder("f_legal", "Legal & Contracts", "gavel", "#8B5CF6", 1, 1729000000000L),
        Folder("f_id", "ID Cards", "badge", "#F59E0B", 1, 1729000000000L)
    )

    private val sampleTags = listOf(
        Tag("t_receipt", "Receipt", "#3B82F6"),
        Tag("t_ocr", "OCR Ready", "#10B981"),
        Tag("t_tax", "Tax 2026", "#F59E0B"),
        Tag("t_legal", "Legal", "#8B5CF6"),
        Tag("t_id", "Identity", "#EC4899")
    )

    private val samplePagesDoc1 = listOf(
        ScannedPage(
            id = "p_101",
            pageNumber = 1,
            originalImagePath = "receipt_orig.jpg",
            processedImagePath = "receipt_magic.jpg",
            cropCorners = QuadCorners(),
            rotationDegrees = 0,
            filterType = FilterType.MAGIC_COLOR_1,
            ocrText = """WHOLE FOODS MARKET
Date: Oct 25, 2026   Inv #84920
1x Organic Oat Milk         $4.99
2x Hass Avocados            $3.50
1x Sourdough Artisan Bread  $5.25
1x Ceremonial Matcha Tea   $12.99
---------------------------------
TOTAL DUE                  $26.73
Tax Included (8.25%)        $2.04
Thank you for shopping at Whole Foods!""",
            createdAt = 1729850000000L
        ),
        ScannedPage(
            id = "p_102",
            pageNumber = 2,
            originalImagePath = "receipt_back.jpg",
            processedImagePath = "receipt_back_clean.jpg",
            cropCorners = QuadCorners(),
            rotationDegrees = 0,
            filterType = FilterType.SHARP_BW,
            ocrText = "Return Policy: 30 days with original receipt.",
            createdAt = 1729850050000L
        )
    )

    private val samplePagesDoc2 = listOf(
        ScannedPage(
            id = "p_201",
            pageNumber = 1,
            originalImagePath = "lease_p1.jpg",
            processedImagePath = "lease_p1_clean.jpg",
            filterType = FilterType.SHARP_BW,
            ocrText = """RESIDENTIAL LEASE AGREEMENT
This agreement entered on October 18, 2026 between LANDLORD and TENANT.
Premises: Apt 4B, 742 Evergreen Terrace.
Monthly Rent: $2,400.00 payable on 1st of month.
Security Deposit: $2,400.00.""",
            createdAt = 1729240000000L
        )
    )

    private val samplePagesDoc3 = listOf(
        ScannedPage(
            id = "p_301",
            pageNumber = 1,
            originalImagePath = "passport_front.jpg",
            processedImagePath = "passport_clean.jpg",
            filterType = FilterType.MAGIC_COLOR_1,
            ocrText = """PASSPORT / PASSEPORT
Type: P   Code: USA   Passport No: A94820194
Surname: DOE   Given Names: JANE ALEXIS
Nationality: UNITED STATES OF AMERICA
Date of Birth: 14 JUL 1994   Sex: F
P<USADOE<<JANE<ALEXIS<<<<<<<<<<<<<<<<<<<<<<<
A948201940USA9407142F3401015<<<<<<<<<<<<<<04""",
            createdAt = 1727700000000L
        )
    )

    private val sampleDocs = listOf(
        Document(
            id = "doc_1",
            title = "Whole Foods Receipt",
            folderId = "f_receipts",
            tags = listOf("Receipt", "OCR Ready", "Tax 2026"),
            pages = samplePagesDoc1,
            createdAt = 1729850000000L,
            updatedAt = 1729850000000L,
            isFavorite = true,
            notes = "Groceries expense for weekend project."
        ),
        Document(
            id = "doc_2",
            title = "Apartment Lease Agreement",
            folderId = "f_legal",
            tags = listOf("Legal", "OCR Ready"),
            pages = samplePagesDoc2,
            createdAt = 1729240000000L,
            updatedAt = 1729240000000L,
            isFavorite = true,
            isEncrypted = false,
            notes = "Signed lease copy for 2026-2027."
        ),
        Document(
            id = "doc_3",
            title = "Passport Photo ID",
            folderId = "f_id",
            tags = listOf("Identity"),
            pages = samplePagesDoc3,
            createdAt = 1727700000000L,
            updatedAt = 1727700000000L,
            isFavorite = false,
            isEncrypted = true,
            notes = "Encrypted passport scan."
        )
    )

    private val _documents = MutableStateFlow<List<Document>>(sampleDocs)
    private val _folders = MutableStateFlow<List<Folder>>(sampleFolders)
    private val _tags = MutableStateFlow<List<Tag>>(sampleTags)

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
