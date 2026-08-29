package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lufick.docscanner.model.Document
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentDetailUiState(
    val document: Document? = null,
    val selectedPageIndex: Int = 0,
    val isRenaming: Boolean = false,
    val isExportModalOpen: Boolean = false
)

class DocumentDetailViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentDetailUiState())
    val uiState: StateFlow<DocumentDetailUiState> = _uiState.asStateFlow()

    fun loadDocument(docId: String) {
        viewModelScope.launch {
            repository.getDocumentById(docId).collect { doc ->
                _uiState.value = _uiState.value.copy(document = doc)
            }
        }
    }

    fun selectPage(index: Int) {
        _uiState.value = _uiState.value.copy(selectedPageIndex = index)
    }

    fun renameDocument(newTitle: String) {
        val docId = _uiState.value.document?.id ?: return
        viewModelScope.launch {
            repository.updateDocumentTitle(docId, newTitle)
        }
    }

    fun deleteCurrentPage() {
        val doc = _uiState.value.document ?: return
        val page = doc.pages.getOrNull(_uiState.value.selectedPageIndex) ?: return
        viewModelScope.launch {
            repository.deletePage(doc.id, page.id)
            val newIdx = (_uiState.value.selectedPageIndex - 1).coerceAtLeast(0)
            _uiState.value = _uiState.value.copy(selectedPageIndex = newIdx)
        }
    }

    fun reorderPages(pages: List<ScannedPage>) {
        val docId = _uiState.value.document?.id ?: return
        viewModelScope.launch {
            repository.reorderPages(docId, pages)
        }
    }
}
