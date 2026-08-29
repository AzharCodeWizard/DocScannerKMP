package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import com.lufick.docscanner.engine.OcrParser
import com.lufick.docscanner.model.ExtractedEntity
import com.lufick.docscanner.model.OcrResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OcrUiState(
    val ocrResult: OcrResult? = null,
    val isRecognizing: Boolean = false,
    val selectedTab: Int = 0, // 0: Text, 1: Structured Entities, 2: Raw Blocks
    val searchQuery: String = "",
    val isCopied: Boolean = false
)

class OcrViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun loadOcrData(rawText: String) {
        val entities = OcrParser.extractEntities(rawText)
        val result = OcrResult(
            fullText = rawText,
            entities = entities,
            detectedLanguage = "en"
        )
        _uiState.value = _uiState.value.copy(ocrResult = result)
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onTextCopied() {
        _uiState.value = _uiState.value.copy(isCopied = true)
    }
}
