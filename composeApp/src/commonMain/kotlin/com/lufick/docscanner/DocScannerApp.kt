package com.lufick.docscanner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lufick.docscanner.engine.DocumentTemplateType
import com.lufick.docscanner.model.Document
import com.lufick.docscanner.model.ScannedPage
import com.lufick.docscanner.model.ScanMode
import com.lufick.docscanner.platform.rememberPlatformImageProcessor
import com.lufick.docscanner.repository.InMemoryDocumentRepository
import com.lufick.docscanner.theme.DocScannerTheme
import com.lufick.docscanner.ui.components.AppLockOverlay
import com.lufick.docscanner.ui.navigation.Screen
import com.lufick.docscanner.ui.screens.CameraScreen
import com.lufick.docscanner.ui.screens.CropScreen
import com.lufick.docscanner.ui.screens.DocumentDetailScreen
import com.lufick.docscanner.ui.screens.FilterScreen
import com.lufick.docscanner.ui.screens.HomeScreen
import com.lufick.docscanner.ui.screens.IdCardScannerScreen
import com.lufick.docscanner.ui.screens.OcrScreen
import com.lufick.docscanner.ui.screens.PdfToolsScreen
import com.lufick.docscanner.ui.screens.QrStudioScreen
import com.lufick.docscanner.ui.screens.SettingsScreen
import com.lufick.docscanner.util.currentTimeMillis
import com.lufick.docscanner.viewmodel.CameraViewModel
import com.lufick.docscanner.viewmodel.CropViewModel
import com.lufick.docscanner.viewmodel.DocumentDetailViewModel
import com.lufick.docscanner.viewmodel.FilterViewModel
import com.lufick.docscanner.viewmodel.HomeViewModel
import com.lufick.docscanner.viewmodel.IdCardViewModel
import com.lufick.docscanner.viewmodel.OcrViewModel
import com.lufick.docscanner.viewmodel.PdfToolsViewModel
import com.lufick.docscanner.viewmodel.QrStudioViewModel
import com.lufick.docscanner.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun DocScannerApp() {
    val navController = rememberNavController()
    val repository = remember { InMemoryDocumentRepository() }
    val scope = rememberCoroutineScope()

    val settingsViewModel = remember { SettingsViewModel() }
    val settingsState by settingsViewModel.uiState.collectAsState()

    val imageProcessor = rememberPlatformImageProcessor()
    val homeViewModel = remember { HomeViewModel(repository) }
    val cameraViewModel = remember { CameraViewModel() }
    val cropViewModel = remember { CropViewModel(imageProcessor) }
    val filterViewModel = remember { FilterViewModel() }
    val detailViewModel = remember { DocumentDetailViewModel(repository) }
    val ocrViewModel = remember { OcrViewModel() }
    val pdfToolsViewModel = remember { PdfToolsViewModel() }
    val idCardViewModel = remember { IdCardViewModel(imageProcessor, repository) }
    val qrStudioViewModel = remember { QrStudioViewModel(repository) }

    DocScannerTheme(
        themeMode = settingsState.themeMode,
        accentTheme = settingsState.accentTheme
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                        onNavigateToIdCard = { navController.navigate(Screen.IdCard.route) },
                        onNavigateToQrStudio = { navController.navigate(Screen.QrStudio.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToDetail = { docId ->
                            navController.navigate(Screen.DocumentDetail.createRoute(docId))
                        }
                    )
                }

                composable(Screen.Camera.route) {
                    CameraScreen(
                        viewModel = cameraViewModel,
                        onClose = { navController.popBackStack() },
                        onNavigateToCrop = {
                            val lastCaptured = cameraViewModel.uiState.value.capturedImages.lastOrNull() ?: "scan_page_1.jpg"
                            val template = when (cameraViewModel.uiState.value.scanMode) {
                                ScanMode.DOCUMENT -> DocumentTemplateType.RECEIPT
                                ScanMode.ID_CARD -> DocumentTemplateType.ID_CARD
                                ScanMode.BOOK -> DocumentTemplateType.LEASE_CONTRACT
                                ScanMode.PASSPORT -> DocumentTemplateType.PASSPORT
                                ScanMode.QR_CODE -> DocumentTemplateType.RECEIPT
                            }
                            val detectedCorners = cameraViewModel.uiState.value.detectedQuad
                            cropViewModel.setImage(lastCaptured, template, initialCorners = detectedCorners)
                            navController.navigate(Screen.Crop.route)
                        }
                    )
                }

                composable(Screen.Crop.route) {
                    CropScreen(
                        viewModel = cropViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToFilter = {
                            val cropState = cropViewModel.uiState.value
                            filterViewModel.setImage(
                                path = cropState.imagePath,
                                template = cropState.templateType,
                                corners = cropState.corners,
                                rotation = cropState.rotationDegrees
                            )
                            navController.navigate(Screen.Filter.route)
                        }
                    )
                }

                composable(Screen.Filter.route) {
                    FilterScreen(
                        viewModel = filterViewModel,
                        onBack = { navController.popBackStack() },
                        onDone = {
                            val filterState = filterViewModel.uiState.value
                            val newDocId = "doc_" + currentTimeMillis()
                            val newPage = ScannedPage(
                                id = "p_" + currentTimeMillis(),
                                pageNumber = 1,
                                originalImagePath = filterState.imagePath,
                                processedImagePath = filterState.imagePath,
                                cropCorners = filterState.corners,
                                rotationDegrees = filterState.rotationDegrees,
                                filterType = filterState.selectedFilter,
                                brightness = filterState.brightness,
                                contrast = filterState.contrast,
                                ocrText = "WHOLE FOODS MARKET\nDate: Oct 25, 2026\nTotal: $37.04",
                                createdAt = currentTimeMillis()
                            )
                            val title = when (filterState.templateType) {
                                DocumentTemplateType.RECEIPT -> "Whole Foods Receipt"
                                DocumentTemplateType.LEASE_CONTRACT -> "Lease Contract"
                                DocumentTemplateType.ID_CARD -> "Driver License ID"
                                DocumentTemplateType.PASSPORT -> "Passport Scan"
                                DocumentTemplateType.MEDICAL_PRESCRIPTION -> "Medical Prescription"
                            }
                            val newDoc = Document(
                                id = newDocId,
                                title = title,
                                folderId = "f_receipts",
                                tags = listOf("New Scan", "OCR Ready"),
                                pages = listOf(newPage),
                                createdAt = currentTimeMillis(),
                                updatedAt = currentTimeMillis(),
                                isFavorite = true
                            )
                            scope.launch {
                                repository.saveDocument(newDoc)
                            }
                            navController.popBackStack(Screen.Home.route, inclusive = false)
                        }
                    )
                }

                composable(Screen.DocumentDetail.route) { backStackEntry ->
                    val docId = backStackEntry.arguments?.getString("docId") ?: "doc_1"
                    DocumentDetailScreen(
                        docId = docId,
                        viewModel = detailViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToOcr = { id -> navController.navigate(Screen.Ocr.createRoute(id)) },
                        onNavigateToPdfTools = { id -> navController.navigate(Screen.PdfTools.createRoute(id)) },
                        onAddPage = { navController.navigate(Screen.Camera.route) }
                    )
                }

                composable(Screen.Ocr.route) { backStackEntry ->
                    val docId = backStackEntry.arguments?.getString("docId") ?: "doc_1"
                    OcrScreen(
                        docId = docId,
                        viewModel = ocrViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.PdfTools.route) { backStackEntry ->
                    val docId = backStackEntry.arguments?.getString("docId") ?: "doc_1"
                    PdfToolsScreen(
                        docId = docId,
                        viewModel = pdfToolsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.IdCard.route) {
                    IdCardScannerScreen(
                        viewModel = idCardViewModel,
                        onBack = { navController.popBackStack() },
                        onDone = { docId ->
                            navController.navigate(Screen.DocumentDetail.createRoute(docId)) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    )
                }

                composable(Screen.QrStudio.route) {
                    QrStudioScreen(
                        viewModel = qrStudioViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToDetail = { docId ->
                            navController.navigate(Screen.DocumentDetail.createRoute(docId)) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // App Lock Biometric / PIN Protection Overlay
            if (settingsState.isAppLockEnabled && !settingsState.isAppUnlocked) {
                AppLockOverlay(
                    isLocked = true,
                    isBiometricEnabled = settingsState.isBiometricEnabled,
                    onUnlockWithPin = { pin -> settingsViewModel.unlockApp(pin) },
                    onUnlockWithBiometric = { settingsViewModel.unlockWithBiometric() }
                )
            }
        }
    }
}
