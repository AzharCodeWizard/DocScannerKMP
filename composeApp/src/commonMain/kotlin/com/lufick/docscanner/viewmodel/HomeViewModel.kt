package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lufick.docscanner.model.Document
import com.lufick.docscanner.model.Folder
import com.lufick.docscanner.model.Tag
import com.lufick.docscanner.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val searchQuery: String = "",
    val selectedFolderId: String = "f_all",
    val selectedTagId: String? = null,
    val isGridView: Boolean = true,
    val isSelectionMode: Boolean = false,
    val selectedDocIds: Set<String> = emptySet()
)

class HomeViewModel(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    val folders: StateFlow<List<Folder>> = repository.getFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tags: StateFlow<List<Tag>> = repository.getTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDocuments: StateFlow<List<Document>> = combine(
        repository.getAllDocuments(),
        _uiState
    ) { docs, state ->
        docs.filter { doc ->
            val matchesFolder = state.selectedFolderId == "f_all" || doc.folderId == state.selectedFolderId
            val matchesQuery = state.searchQuery.isEmpty() ||
                    doc.title.contains(state.searchQuery, ignoreCase = true) ||
                    doc.pages.any { it.ocrText?.contains(state.searchQuery, ignoreCase = true) == true }
            val matchesTag = state.selectedTagId == null || doc.tags.contains(state.selectedTagId)
            matchesFolder && matchesQuery && matchesTag
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectFolder(folderId: String) {
        _uiState.value = _uiState.value.copy(selectedFolderId = folderId)
    }

    fun selectTag(tagId: String?) {
        _uiState.value = _uiState.value.copy(selectedTagId = tagId)
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isGridView = !_uiState.value.isGridView)
    }

    fun toggleFavorite(docId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(docId)
        }
    }

    fun deleteDocument(docId: String) {
        viewModelScope.launch {
            repository.deleteDocument(docId)
        }
    }
}
