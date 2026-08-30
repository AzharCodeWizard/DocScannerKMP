package com.lufick.docscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lufick.docscanner.model.ContactCard
import com.lufick.docscanner.model.Document
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QrContentType
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.model.UpiConfig
import com.lufick.docscanner.model.WifiConfig
import com.lufick.docscanner.repository.DocumentRepository
import com.lufick.docscanner.util.QrCodeGenerator
import com.lufick.docscanner.util.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class QrStudioTab { GENERATE, SCAN }

data class QrStudioUiState(
    val selectedTab: QrStudioTab = QrStudioTab.GENERATE,
    val contentType: QrContentType = QrContentType.URL,
    val urlInput: String = "https://docscanner.in",
    val textInput: String = "Welcome to Lufick DocScanner!",
    val wifiConfig: WifiConfig = WifiConfig(ssid = "Office_WiFi", password = "SecurePassword123"),
    val contactCard: ContactCard = ContactCard(name = "Alex Mercer", phone = "+1 (555) 234-5678", email = "alex@example.com", organization = "DocScanner Inc"),
    val upiConfig: UpiConfig = UpiConfig(vpa = "scanner@upi", payeeName = "DocScanner Pro", amount = "499"),
    val currentPayload: String = "https://docscanner.in",
    val qrMatrix: Array<BooleanArray> = QrCodeGenerator.generateQrMatrix("https://docscanner.in"),
    val scannedContent: String? = null,
    val isSavedToVault: Boolean = false,
    val flashEnabled: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val autoZoomActive: Boolean = true,
    val detectedQrBoundingRatio: Float = 0f
)

class QrStudioViewModel(private val repository: DocumentRepository? = null) : ViewModel() {

    private val _uiState = MutableStateFlow(QrStudioUiState())
    val uiState: StateFlow<QrStudioUiState> = _uiState.asStateFlow()

    fun setTab(tab: QrStudioTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(flashEnabled = !_uiState.value.flashEnabled)
    }

    fun setZoom(ratio: Float) {
        _uiState.value = _uiState.value.copy(zoomRatio = ratio)
    }

    fun toggleAutoZoom() {
        _uiState.value = _uiState.value.copy(autoZoomActive = !_uiState.value.autoZoomActive)
    }

    fun setContentType(type: QrContentType) {
        val payload = computePayload(
            type,
            _uiState.value.urlInput,
            _uiState.value.textInput,
            _uiState.value.wifiConfig,
            _uiState.value.contactCard,
            _uiState.value.upiConfig
        )
        val matrix = QrCodeGenerator.generateQrMatrix(payload)
        _uiState.value = _uiState.value.copy(
            contentType = type,
            currentPayload = payload,
            qrMatrix = matrix
        )
    }

    fun updateUrl(url: String) {
        val matrix = QrCodeGenerator.generateQrMatrix(url)
        _uiState.value = _uiState.value.copy(urlInput = url, currentPayload = url, qrMatrix = matrix)
    }

    fun updateText(text: String) {
        val matrix = QrCodeGenerator.generateQrMatrix(text)
        _uiState.value = _uiState.value.copy(textInput = text, currentPayload = text, qrMatrix = matrix)
    }

    fun updateWifi(wifi: WifiConfig) {
        val payload = wifi.toQrPayload()
        val matrix = QrCodeGenerator.generateQrMatrix(payload)
        _uiState.value = _uiState.value.copy(wifiConfig = wifi, currentPayload = payload, qrMatrix = matrix)
    }

    fun updateContact(contact: ContactCard) {
        val payload = contact.toVCardPayload()
        val matrix = QrCodeGenerator.generateQrMatrix(payload)
        _uiState.value = _uiState.value.copy(contactCard = contact, currentPayload = payload, qrMatrix = matrix)
    }

    fun updateUpi(upi: UpiConfig) {
        val payload = upi.toUpiPayload()
        val matrix = QrCodeGenerator.generateQrMatrix(payload)
        _uiState.value = _uiState.value.copy(upiConfig = upi, currentPayload = payload, qrMatrix = matrix)
    }

    fun onScanned(rawContent: String, qrBoundingRatio: Float = 0.5f) {
        _uiState.value = _uiState.value.copy(
            scannedContent = rawContent,
            detectedQrBoundingRatio = qrBoundingRatio
        )
    }

    fun clearScanned() {
        _uiState.value = _uiState.value.copy(scannedContent = null, detectedQrBoundingRatio = 0f)
    }

    fun saveQrToVault(title: String, onSaved: (String) -> Unit) {
        val repo = repository ?: return
        val payload = _uiState.value.currentPayload

        viewModelScope.launch {
            val now = currentTimeMillis()
            val docId = "qr_doc_$now"
            val page = ScannedPage(
                id = "p_$now",
                pageNumber = 1,
                originalImagePath = "",
                processedImagePath = "",
                cropCorners = QuadCorners(PointF(0f, 0f), PointF(1f, 0f), PointF(1f, 1f), PointF(0f, 1f)),
                rotationDegrees = 0,
                filterType = FilterType.ORIGINAL,
                brightness = 0f,
                contrast = 1f,
                ocrText = "QR CODE / BARCODE DATA\nType: ${_uiState.value.contentType.displayName}\nPayload:\n$payload",
                createdAt = now
            )

            val doc = Document(
                id = docId,
                title = title.ifBlank { "QR Code - ${_uiState.value.contentType.displayName}" },
                folderId = "f_all",
                tags = listOf("QR Code", _uiState.value.contentType.displayName),
                pages = listOf(page),
                createdAt = now,
                updatedAt = now,
                isFavorite = false
            )

            repo.saveDocument(doc)
            _uiState.value = _uiState.value.copy(isSavedToVault = true)
            onSaved(docId)
        }
    }

    fun saveScannedToVault(title: String, onSaved: (String) -> Unit) {
        val repo = repository ?: return
        val payload = _uiState.value.scannedContent ?: return

        viewModelScope.launch {
            val now = currentTimeMillis()
            val docId = "scan_qr_$now"
            val page = ScannedPage(
                id = "p_scanned_$now",
                pageNumber = 1,
                originalImagePath = "",
                processedImagePath = "",
                cropCorners = QuadCorners(PointF(0f, 0f), PointF(1f, 0f), PointF(1f, 1f), PointF(0f, 1f)),
                rotationDegrees = 0,
                filterType = FilterType.ORIGINAL,
                brightness = 0f,
                contrast = 1f,
                ocrText = "SCANNED QR / BARCODE RESULT\nContent:\n$payload",
                createdAt = now
            )

            val doc = Document(
                id = docId,
                title = title.ifBlank { "Scanned QR Code" },
                folderId = "f_all",
                tags = listOf("Scanned QR", "Barcode"),
                pages = listOf(page),
                createdAt = now,
                updatedAt = now,
                isFavorite = false
            )

            repo.saveDocument(doc)
            onSaved(docId)
        }
    }

    private fun computePayload(
        type: QrContentType,
        url: String,
        text: String,
        wifi: WifiConfig,
        contact: ContactCard,
        upi: UpiConfig
    ): String {
        return when (type) {
            QrContentType.URL -> url
            QrContentType.TEXT -> text
            QrContentType.WIFI -> wifi.toQrPayload()
            QrContentType.CONTACT -> contact.toVCardPayload()
            QrContentType.UPI -> upi.toUpiPayload()
        }
    }
}
