package com.lufick.docscanner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.model.ContactCard
import com.lufick.docscanner.model.QrContentType
import com.lufick.docscanner.model.UpiConfig
import com.lufick.docscanner.model.WifiConfig
import com.lufick.docscanner.platform.CameraPreview
import com.lufick.docscanner.platform.PlatformCameraHandler
import com.lufick.docscanner.platform.rememberPlatformShare
import com.lufick.docscanner.theme.LufickEmerald
import com.lufick.docscanner.ui.components.LufickTopBar
import com.lufick.docscanner.viewmodel.QrStudioTab
import com.lufick.docscanner.viewmodel.QrStudioViewModel

@Composable
fun QrStudioScreen(
    viewModel: QrStudioViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (docId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val platformShare = rememberPlatformShare()
    var copiedFeedback by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LufickTopBar(
                title = "QR & Barcode Studio",
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row (Generator / Scanner)
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = LufickEmerald,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                        color = LufickEmerald
                    )
                }
            ) {
                Tab(
                    selected = uiState.selectedTab == QrStudioTab.GENERATE,
                    onClick = { viewModel.setTab(QrStudioTab.GENERATE) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QR Generator", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = uiState.selectedTab == QrStudioTab.SCAN,
                    onClick = { viewModel.setTab(QrStudioTab.SCAN) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QR Scanner", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }

            if (uiState.selectedTab == QrStudioTab.GENERATE) {
                // Generator Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Type Selector Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QrContentType.entries) { type ->
                            val isSelected = uiState.contentType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) LufickEmerald else MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        if (isSelected) LufickEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setContentType(type) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Input Form Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        when (uiState.contentType) {
                            QrContentType.URL -> {
                                OutlinedTextField(
                                    value = uiState.urlInput,
                                    onValueChange = { viewModel.updateUrl(it) },
                                    label = { Text("Website URL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            QrContentType.TEXT -> {
                                OutlinedTextField(
                                    value = uiState.textInput,
                                    onValueChange = { viewModel.updateText(it) },
                                    label = { Text("Plain Text Content") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            QrContentType.WIFI -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = uiState.wifiConfig.ssid,
                                        onValueChange = { viewModel.updateWifi(uiState.wifiConfig.copy(ssid = it)) },
                                        label = { Text("Network SSID") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = uiState.wifiConfig.password,
                                        onValueChange = { viewModel.updateWifi(uiState.wifiConfig.copy(password = it)) },
                                        label = { Text("Wi-Fi Password") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            QrContentType.CONTACT -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = uiState.contactCard.name,
                                        onValueChange = { viewModel.updateContact(uiState.contactCard.copy(name = it)) },
                                        label = { Text("Contact Full Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = uiState.contactCard.phone,
                                        onValueChange = { viewModel.updateContact(uiState.contactCard.copy(phone = it)) },
                                        label = { Text("Phone Number") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = uiState.contactCard.email,
                                        onValueChange = { viewModel.updateContact(uiState.contactCard.copy(email = it)) },
                                        label = { Text("Email Address") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            QrContentType.UPI -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = uiState.upiConfig.vpa,
                                        onValueChange = { viewModel.updateUpi(uiState.upiConfig.copy(vpa = it)) },
                                        label = { Text("UPI ID / VPA (e.g. name@upi)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = uiState.upiConfig.payeeName,
                                        onValueChange = { viewModel.updateUpi(uiState.upiConfig.copy(payeeName = it)) },
                                        label = { Text("Payee Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = uiState.upiConfig.amount,
                                        onValueChange = { viewModel.updateUpi(uiState.upiConfig.copy(amount = it)) },
                                        label = { Text("Amount (INR)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // QR Vector Preview Card
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(3.dp, LufickEmerald, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val matrix = uiState.qrMatrix
                            val rows = matrix.size
                            val cols = matrix[0].size
                            val cellW = size.width / cols
                            val cellH = size.height / rows

                            for (r in 0 until rows) {
                                for (c in 0 until cols) {
                                    if (matrix[r][c]) {
                                        drawRect(
                                            color = Color(0xFF0F172A),
                                            topLeft = Offset(c * cellW, r * cellH),
                                            size = Size(cellW + 0.5f, cellH + 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Raw Payload Snippet
                    Text(
                        text = uiState.currentPayload,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(uiState.currentPayload))
                                copiedFeedback = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (copiedFeedback) "Copied!" else "Copy", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = {
                                platformShare.shareText(uiState.currentPayload)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.saveQrToVault("QR_${uiState.contentType.displayName}") { docId ->
                                    onNavigateToDetail(docId)
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LufickEmerald)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Vault", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Google Pay Auto-Zooming QR Scanner View
                var cameraHandler by remember { mutableStateOf<PlatformCameraHandler?>(null) }
                val infiniteTransition = rememberInfiniteTransition()
                val laserProgress by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // 1. Live Camera Preview with Auto-Zoom Analyzer
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        flashEnabled = uiState.flashEnabled,
                        isQrScanMode = true,
                        onQrDetected = { payload, ratio ->
                            viewModel.onScanned(payload, ratio)
                        },
                        onCameraBind = { handler -> cameraHandler = handler }
                    )

                    // 2. Top Viewfinder HUD Overlay (Auto-Zoom Badge + Flash Toggle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Google Pay Auto-Zoom Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, LufickEmerald.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LufickEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-Zoom to QR (Google Pay)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Torch Button
                        IconButton(
                            onClick = { viewModel.toggleFlash() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .size(40.dp)
                        ) {
                            Icon(
                                if (uiState.flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = if (uiState.flashEnabled) LufickEmerald else Color.White
                            )
                        }
                    }

                    // 3. Central Google Pay Scanning Reticle
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(280.dp)
                            .border(3.dp, LufickEmerald, RoundedCornerShape(24.dp))
                    ) {
                        // Animated Scanning Laser Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = (280.dp * laserProgress).coerceIn(0.dp, 276.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            LufickEmerald,
                                            Color.White,
                                            LufickEmerald,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // 4. Quick Manual Zoom Controls Pill (1x, 2x, 3.5x)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (uiState.scannedContent != null) 220.dp else 120.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1.0f, 2.0f, 3.5f).forEach { zoom ->
                            val isSelected = uiState.zoomRatio == zoom
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) LufickEmerald else Color.Transparent)
                                    .clickable {
                                        viewModel.setZoom(zoom)
                                        cameraHandler?.setZoom(zoom)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${zoom}x",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 5. Scanned Result Bottom Card (Google Pay Style)
                    val scanned = uiState.scannedContent
                    if (scanned != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.5.dp, LufickEmerald, RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = LufickEmerald,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "QR Code Detected",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.clearScanned()
                                            cameraHandler?.resetZoom()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Content snippet box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = scanned,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 12.sp,
                                        maxLines = 4
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(scanned))
                                            copiedFeedback = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (copiedFeedback) "Copied" else "Copy", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { platformShare.shareText(scanned) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveScannedToVault("Scanned_QR") { docId ->
                                                onNavigateToDetail(docId)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LufickEmerald),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save Vault", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 6. If no QR scanned yet, show helper hint at bottom
                    if (uiState.scannedContent == null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Point camera at any QR Code or Barcode",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.onScanned("https://docscanner.in/pay?amt=499&ref=TXN99281")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LufickEmerald),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AutoMode, contentDescription = null, tint = LufickEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simulate Auto-Zoom & Scan", color = LufickEmerald, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
