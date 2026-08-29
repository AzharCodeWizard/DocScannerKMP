package com.lufick.docscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.lufick.docscanner.theme.LufickEmerald
import com.lufick.docscanner.ui.components.LufickTopBar
import com.lufick.docscanner.viewmodel.DocumentDetailViewModel

@Composable
fun DocumentDetailScreen(
    docId: String,
    viewModel: DocumentDetailViewModel,
    onBack: () -> Unit,
    onNavigateToOcr: (String) -> Unit,
    onNavigateToPdfTools: (String) -> Unit,
    onAddPage: () -> Unit
) {
    LaunchedEffect(docId) {
        viewModel.loadDocument(docId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val doc = uiState.document

    Scaffold(
        topBar = {
            LufickTopBar(
                title = doc?.title ?: "Document",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { onNavigateToPdfTools(docId) }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Tools", tint = LufickEmerald)
                    }
                    IconButton(onClick = { onNavigateToOcr(docId) }) {
                        Icon(Icons.Default.TextFields, contentDescription = "OCR Text", tint = LufickEmerald)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (doc == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading document...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Large Active Page Preview
                val activePage = doc.pages.getOrNull(uiState.selectedPageIndex)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${doc.title} - Page ${uiState.selectedPageIndex + 1}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                        if (!activePage?.ocrText.isNullOrEmpty()) {
                            Text(
                                text = activePage?.ocrText ?: "",
                                color = Color.DarkGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        } else {
                            Text("High resolution scanned page", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(
                            text = "Filter: ${activePage?.filterType?.displayName ?: "Magic Color"}",
                            fontSize = 10.sp,
                            color = LufickEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-Page Thumbnails Strip
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pages (${doc.pageCount})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(doc.pages) { idx, page ->
                            val isSelected = uiState.selectedPageIndex == idx
                            Box(
                                modifier = Modifier
                                    .size(68.dp, 90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(
                                        2.dp,
                                        if (isSelected) LufickEmerald else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectPage(idx) }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("P${idx + 1}", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                                    Text(page.filterType.displayName.take(5), fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                        }

                        item {
                            // Add Page Card
                            Box(
                                modifier = Modifier
                                    .size(68.dp, 90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { onAddPage() },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = LufickEmerald)
                                    Text("Add", fontSize = 10.sp, color = LufickEmerald)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Bar (PDF, OCR, Share, Delete Page)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { onNavigateToPdfTools(docId) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = LufickEmerald)
                            Text("PDF Tools", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = { onNavigateToOcr(docId) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.TextFields, contentDescription = null, tint = LufickEmerald)
                            Text("OCR Text", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = { viewModel.deleteCurrentPage() }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            Text("Delete Page", fontSize = 10.sp, color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}
