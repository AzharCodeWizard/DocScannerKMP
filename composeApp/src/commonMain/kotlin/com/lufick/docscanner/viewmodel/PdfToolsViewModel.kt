package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import com.lufick.docscanner.model.PageOrientation
import com.lufick.docscanner.model.PageSize
import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.PdfQuality
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.SignatureData
import com.lufick.docscanner.model.WatermarkConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PdfToolsUiState(
    val config: PdfConfig = PdfConfig(),
    val estimatedSizeKb: Int = 340,
    val isExporting: Boolean = false,
    val exportedFilePath: String? = null
)

class PdfToolsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PdfToolsUiState())
    val uiState: StateFlow<PdfToolsUiState> = _uiState.asStateFlow()

    fun setPageSize(size: PageSize) {
        val newConfig = _uiState.value.config.copy(pageSize = size)
        _uiState.value = _uiState.value.copy(config = newConfig)
    }

    fun setOrientation(orientation: PageOrientation) {
        val newConfig = _uiState.value.config.copy(orientation = orientation)
        _uiState.value = _uiState.value.copy(config = newConfig)
    }

    fun setQuality(quality: PdfQuality) {
        val newConfig = _uiState.value.config.copy(quality = quality)
        val size = when (quality) {
            PdfQuality.HIGH -> 850
            PdfQuality.BALANCED -> 340
            PdfQuality.LOW -> 120
        }
        _uiState.value = _uiState.value.copy(config = newConfig, estimatedSizeKb = size)
    }

    fun setWatermark(enabled: Boolean, text: String, opacity: Float = 0.25f) {
        val newWm = WatermarkConfig(isEnabled = enabled, text = text, opacity = opacity)
        val newConfig = _uiState.value.config.copy(watermark = newWm)
        _uiState.value = _uiState.value.copy(config = newConfig)
    }

    fun setPassword(password: String?) {
        val newConfig = _uiState.value.config.copy(passwordProtection = password)
        _uiState.value = _uiState.value.copy(config = newConfig)
    }

    fun setSignature(points: List<PointF>) {
        val sig = SignatureData(isEnabled = points.isNotEmpty(), points = points)
        val newConfig = _uiState.value.config.copy(signature = sig)
        _uiState.value = _uiState.value.copy(config = newConfig)
    }
}
