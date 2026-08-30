package com.lufick.docscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.platform.PlatformOcrEngine
import com.lufick.docscanner.platform.rememberPlatformOcrEngine
import com.lufick.docscanner.platform.rememberPlatformShare
import com.lufick.docscanner.repository.DocumentRepository
import com.lufick.docscanner.ui.components.LufickTopBar
import com.lufick.docscanner.viewmodel.OcrViewModel
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun OcrScreen(
    docId: String,
    viewModel: OcrViewModel,
    repository: DocumentRepository,
    ocrEngine: PlatformOcrEngine = rememberPlatformOcrEngine(),
    onBack: () -> Unit
) {
    val platformShare = rememberPlatformShare()

    LaunchedEffect(docId) {
        val doc = repository.getDocumentById(docId).firstOrNull()
        val firstPage = doc?.pages?.firstOrNull()
        if (firstPage != null) {
            if (!firstPage.ocrText.isNullOrBlank()) {
                viewModel.loadOcrData(firstPage.ocrText)
            } else {
                val imagePath = firstPage.processedImagePath.ifBlank { firstPage.originalImagePath }
                if (imagePath.isNotBlank()) {
                    val result = ocrEngine.recognizeText(imagePath)
                    viewModel.loadOcrData(result.fullText)
                    if (result.fullText.isNotBlank()) {
                        repository.updatePage(docId, firstPage.copy(ocrText = result.fullText))
                    }
                } else {
                    viewModel.loadOcrData("No image found for this document page.")
                }
            }
        } else {
            viewModel.loadOcrData("Document not found.")
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val ocr = uiState.ocrResult

    Scaffold(
        topBar = {
            LufickTopBar(
                title = "AI OCR & Smart Entities",
                onBackClick = onBack,
                actions = {
                    Button(
                        onClick = { viewModel.onTextCopied() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black)
                        Text(if (uiState.isCopied) "Copied!" else "Copy", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tabs (Structured Entities vs Raw Text)
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Smart Extracted Data", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Full Text Editor", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            if (uiState.selectedTab == 0) {
                // Structured Entities List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val entities = ocr?.entities ?: emptyList()
                    items(entities) { entity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(entity.key, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(entity.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(entity.category, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Raw OCR Text Editor
                OutlinedTextField(
                    value = ocr?.fullText ?: "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Export Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        ocr?.fullText?.let { text ->
                            platformShare.shareText("Extracted OCR Text:\n\n$text")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text("Share Text", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = {
                        viewModel.onTextCopied()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (uiState.isCopied) "Copied!" else "Copy All", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
