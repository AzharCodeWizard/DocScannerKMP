package com.lufick.docscanner.repository

import com.lufick.docscanner.model.Document
import com.lufick.docscanner.model.Folder
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.model.Tag
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<Document>>
    fun getDocumentById(id: String): Flow<Document?>
    fun getFolders(): Flow<List<Folder>>
    fun getTags(): Flow<List<Tag>>
    
    suspend fun saveDocument(doc: Document)
    suspend fun deleteDocument(id: String)
    suspend fun toggleFavorite(id: String)
    suspend fun updateDocumentTitle(id: String, newTitle: String)
    suspend fun addPageToDocument(documentId: String, page: ScannedPage)
    suspend fun updatePage(documentId: String, page: ScannedPage)
    suspend fun reorderPages(documentId: String, newPages: List<ScannedPage>)
    suspend fun deletePage(documentId: String, pageId: String)
    
    suspend fun createFolder(name: String, colorHex: String = "#10B981")
    suspend fun deleteFolder(id: String)
    suspend fun createTag(name: String, colorHex: String = "#3B82F6")
}
