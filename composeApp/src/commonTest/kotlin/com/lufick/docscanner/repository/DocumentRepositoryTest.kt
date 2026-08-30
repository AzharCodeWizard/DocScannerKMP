package com.lufick.docscanner.repository

import com.lufick.docscanner.model.Document
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentRepositoryTest {

    @Test
    fun testRepositoryOperations() = runTest {
        val repo = InMemoryDocumentRepository()
        val initialDocs = repo.getAllDocuments().first()
        assertTrue(initialDocs.isEmpty())

        val newDoc = Document(
            id = "test_doc_1",
            title = "Test Receipt",
            pages = emptyList()
        )
        repo.saveDocument(newDoc)

        val retrieved = repo.getDocumentById("test_doc_1").first()
        assertEquals("Test Receipt", retrieved?.title)

        repo.toggleFavorite("test_doc_1")
        val favDoc = repo.getDocumentById("test_doc_1").first()
        assertTrue(favDoc?.isFavorite == true)

        repo.deleteDocument("test_doc_1")
        val deletedDoc = repo.getDocumentById("test_doc_1").first()
        assertEquals(null, deletedDoc)
    }
}
