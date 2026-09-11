package com.lufick.docscanner.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

private const val TAG = "DocScannerCamera"

// Stable edge detector state to eliminate frame-to-frame flicker
private var lastQuadCorners: QuadCorners? = null
private var lastAnalysisTimestamp: Long = 0L

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    flashEnabled: Boolean,
    zoomRatio: Float,
    isQrScanMode: Boolean,
    onEdgeDetected: (QuadCorners) -> Unit,
    onQrDetected: (payload: String, qrBoundingRatio: Float) -> Unit,
    onCameraBind: (PlatformCameraHandler) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        Log.d(TAG, "Camera permission result: $isGranted")
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0B0F19))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera Permission Required", color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    } else {
        var cameraInstance by remember { mutableStateOf<Camera?>(null) }
        val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }

        val barcodeScanner = remember {
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_AZTEC,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_UPC_A
                )
                .build()
            BarcodeScanning.getClient(options)
        }

        LaunchedEffect(imageCapture, cameraInstance) {
            onCameraBind(AndroidPlatformCameraHandler(context, imageCapture, cameraInstance))
        }

        AndroidView(
            modifier = modifier.fillMaxSize().background(Color.Black),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                val analysisExecutor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                    try {
                                        val now = System.currentTimeMillis()
                                        if (isQrScanMode) {
                                            analyzeQrAndAutoZoom(imageProxy, barcodeScanner, cameraInstance, onQrDetected)
                                        } else {
                                            // Throttle edge detection to ~10 FPS (every 100ms) to prevent CPU hogging and jitter
                                            if (now - lastAnalysisTimestamp >= 100L) {
                                                lastAnalysisTimestamp = now
                                                val detectedQuad = analyzeDocumentEdges(imageProxy)
                                                onEdgeDetected(detectedQuad)
                                            }
                                            imageProxy.close()
                                        }
                                    } catch (e: Exception) {
                                        try { imageProxy.close() } catch (_: Exception) {}
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
                            imageAnalyzer
                        )
                        cameraInstance = camera
                        camera.cameraControl.enableTorch(flashEnabled)
                        val zoomState = camera.cameraInfo.zoomState.value
                        val minZ = zoomState?.minZoomRatio ?: 1.0f
                        val maxZ = zoomState?.maxZoomRatio ?: 5.0f
                        camera.cameraControl.setZoomRatio(zoomRatio.coerceIn(minZ, maxZ))
                        
                        onCameraBind(AndroidPlatformCameraHandler(context, imageCapture, camera))
                    } catch (exc: Exception) {
                        Log.e(TAG, "Camera binding error", exc)
                    }
                }, executor)

                previewView
            },
            update = {
                try {
                    cameraInstance?.cameraControl?.enableTorch(flashEnabled)
                    cameraInstance?.let { cam ->
                        val zoomState = cam.cameraInfo.zoomState.value
                        val minZ = zoomState?.minZoomRatio ?: 1.0f
                        val maxZ = zoomState?.maxZoomRatio ?: 5.0f
                        cam.cameraControl.setZoomRatio(zoomRatio.coerceIn(minZ, maxZ))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Camera control update error", e)
                }
            }
        )
    }
}

@OptIn(ExperimentalGetImage::class)
private fun analyzeQrAndAutoZoom(
    imageProxy: androidx.camera.core.ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    camera: Camera?,
    onQrDetected: (String, Float) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val rotation = imageProxy.imageInfo.rotationDegrees
    val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

    barcodeScanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val qr = barcodes.firstOrNull()
            if (qr != null && !qr.rawValue.isNullOrBlank()) {
                val box = qr.boundingBox
                val isRotated = (rotation == 90 || rotation == 270)
                val frameW = if (isRotated) imageProxy.height else imageProxy.width
                val frameH = if (isRotated) imageProxy.width else imageProxy.height

                val qrWidth = box?.width()?.toFloat() ?: (frameW * 0.25f)
                val qrHeight = box?.height()?.toFloat() ?: (frameH * 0.25f)
                val maxDim = max(qrWidth, qrHeight)
                val ratio = (maxDim / frameW.toFloat()).coerceIn(0.01f, 1.0f)

                // Google Pay Auto-Zoom Algorithm:
                if (ratio < 0.32f && camera != null) {
                    val zoomState = camera.cameraInfo.zoomState.value
                    val currentZoom = zoomState?.zoomRatio ?: 1f
                    val maxZoom = (zoomState?.maxZoomRatio ?: 4f).coerceAtMost(4.5f)
                    
                    val targetZoom = (currentZoom * (0.45f / ratio.coerceAtLeast(0.08f))).coerceIn(1.0f, maxZoom)
                    camera.cameraControl.setZoomRatio(targetZoom)
                }

                onQrDetected(qr.rawValue!!, ratio)
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

class AndroidPlatformCameraHandler(
    private val context: Context,
    private val imageCapture: ImageCapture? = null,
    private val camera: Camera? = null
) : PlatformCameraHandler {
    
    override fun capturePhoto(onPhotoCaptured: (imagePath: String) -> Unit) {
        if (imageCapture == null) {
            onPhotoCaptured("${context.filesDir}/scan_${System.currentTimeMillis()}.jpg")
            return
        }

        val photoFile = File(context.filesDir, "scan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onPhotoCaptured(photoFile.absolutePath)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exc)
                }
            }
        )
    }

    override fun toggleFlash(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    override fun setZoom(ratio: Float) {
        camera?.let { cam ->
            try {
                val zoomState = cam.cameraInfo.zoomState.value
                val minZ = zoomState?.minZoomRatio ?: 1.0f
                val maxZ = zoomState?.maxZoomRatio ?: 5.0f
                val safeRatio = ratio.coerceIn(minZ, maxZ)
                cam.cameraControl.setZoomRatio(safeRatio)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set camera zoom ratio: $ratio", e)
            }
        }
    }

    override fun resetZoom() {
        camera?.let { cam ->
            try {
                cam.cameraControl.setZoomRatio(1.0f)
            } catch (_: Exception) {}
        }
    }
}

/**
 * High-Precision, Jitter-Free Document Edge Detector
 * Uses 2D luminance contrast gradient scanning + Exponential Moving Average (EMA)
 * and deadband thresholding to completely eliminate flicker and coordinate shaking.
 */
private fun analyzeDocumentEdges(imageProxy: androidx.camera.core.ImageProxy): QuadCorners {
    val plane = imageProxy.planes[0]
    val buffer = plane.buffer
    val rawW = imageProxy.width
    val rawH = imageProxy.height
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride
    val rotation = imageProxy.imageInfo.rotationDegrees

    // Is image oriented vertically in sensor?
    val isPortrait = (rotation == 90 || rotation == 270)
    val displayW = if (isPortrait) rawH else rawW
    val displayH = if (isPortrait) rawW else rawH

    // Sample a 32x40 luminance grid aligned to portrait display coordinates
    val gridCols = 32
    val gridRows = 40
    val lumGrid = Array(gridRows) { IntArray(gridCols) }

    for (r in 0 until gridRows) {
        val normY = (r + 0.5f) / gridRows.toFloat()
        for (c in 0 until gridCols) {
            val normX = (c + 0.5f) / gridCols.toFloat()

            // Map display (normX, normY) to raw sensor (srcX, srcY)
            val (srcX, srcY) = when (rotation) {
                90 -> Pair((normY * (rawW - 1)).toInt(), ((1f - normX) * (rawH - 1)).toInt())
                270 -> Pair(((1f - normY) * (rawW - 1)).toInt(), (normX * (rawH - 1)).toInt())
                180 -> Pair(((1f - normX) * (rawW - 1)).toInt(), ((1f - normY) * (rawH - 1)).toInt())
                else -> Pair((normX * (rawW - 1)).toInt(), (normY * (rawH - 1)).toInt())
            }

            val clampedX = srcX.coerceIn(0, rawW - 1)
            val clampedY = srcY.coerceIn(0, rawH - 1)
            val index = clampedY * rowStride + clampedX * pixelStride

            if (index < buffer.limit()) {
                lumGrid[r][c] = buffer.get(index).toInt() and 0xFF
            }
        }
    }

    // Default centered document framing box
    val defaultMinX = 0.08f
    val defaultMaxX = 0.92f
    val defaultMinY = 0.12f
    val defaultMaxY = 0.68f

    // 1. Scan Left Edge (from 4% to 38% width)
    var bestLeftCol = (gridCols * defaultMinX).toInt()
    var maxLeftGrad = 0
    for (c in (gridCols * 0.04f).toInt() until (gridCols * 0.38f).toInt()) {
        var colGrad = 0
        for (r in (gridRows * 0.15f).toInt() until (gridRows * 0.70f).toInt()) {
            colGrad += abs(lumGrid[r][c + 1] - lumGrid[r][c - 1])
        }
        if (colGrad > maxLeftGrad) {
            maxLeftGrad = colGrad
            bestLeftCol = c
        }
    }

    // 2. Scan Right Edge (from 62% to 96% width)
    var bestRightCol = (gridCols * defaultMaxX).toInt()
    var maxRightGrad = 0
    for (c in (gridCols * 0.62f).toInt() until (gridCols * 0.96f).toInt()) {
        var colGrad = 0
        for (r in (gridRows * 0.15f).toInt() until (gridRows * 0.70f).toInt()) {
            colGrad += abs(lumGrid[r][c + 1] - lumGrid[r][c - 1])
        }
        if (colGrad > maxRightGrad) {
            maxRightGrad = colGrad
            bestRightCol = c
        }
    }

    // 3. Scan Top Edge (from 6% to 32% height)
    var bestTopRow = (gridRows * defaultMinY).toInt()
    var maxTopGrad = 0
    for (r in (gridRows * 0.06f).toInt() until (gridRows * 0.32f).toInt()) {
        var rowGrad = 0
        for (c in (gridCols * 0.15f).toInt() until (gridCols * 0.85f).toInt()) {
            rowGrad += abs(lumGrid[r + 1][c] - lumGrid[r - 1][c])
        }
        if (rowGrad > maxTopGrad) {
            maxTopGrad = rowGrad
            bestTopRow = r
        }
    }

    // 4. Scan Bottom Edge (from 50% to 85% height)
    var bestBottomRow = (gridRows * defaultMaxY).toInt()
    var maxBottomGrad = 0
    for (r in (gridRows * 0.50f).toInt() until (gridRows * 0.85f).toInt()) {
        var rowGrad = 0
        for (c in (gridCols * 0.15f).toInt() until (gridCols * 0.85f).toInt()) {
            rowGrad += abs(lumGrid[r + 1][c] - lumGrid[r - 1][c])
        }
        if (rowGrad > maxBottomGrad) {
            maxBottomGrad = rowGrad
            bestBottomRow = r
        }
    }

    // Quality check: Do we have sufficient edge contrast to believe a real document exists?
    val totalConfidence = (maxLeftGrad + maxRightGrad + maxTopGrad + maxBottomGrad)
    val hasStrongEdges = totalConfidence > 1200

    val detectedMinX: Float
    val detectedMaxX: Float
    val detectedMinY: Float
    val detectedMaxY: Float

    if (hasStrongEdges) {
        detectedMinX = (bestLeftCol.toFloat() / gridCols).coerceIn(0.05f, 0.28f)
        detectedMaxX = (bestRightCol.toFloat() / gridCols).coerceIn(0.72f, 0.95f)
        detectedMinY = (bestTopRow.toFloat() / gridRows).coerceIn(0.08f, 0.28f)
        detectedMaxY = (bestBottomRow.toFloat() / gridRows).coerceIn(0.55f, 0.78f)
    } else {
        detectedMinX = defaultMinX
        detectedMaxX = defaultMaxX
        detectedMinY = defaultMinY
        detectedMaxY = defaultMaxY
    }

    val rawQuad = QuadCorners(
        topLeft = PointF(detectedMinX, detectedMinY),
        topRight = PointF(detectedMaxX, detectedMinY),
        bottomRight = PointF(detectedMaxX, detectedMaxY),
        bottomLeft = PointF(detectedMinX, detectedMaxY)
    )

    // Apply Exponential Moving Average (EMA) & Deadband Filtering
    val prev = lastQuadCorners
    if (prev == null) {
        lastQuadCorners = rawQuad
        return rawQuad
    }

    // Calculate maximum point movement
    val dTL = hypot(rawQuad.topLeft.x - prev.topLeft.x, rawQuad.topLeft.y - prev.topLeft.y)
    val dTR = hypot(rawQuad.topRight.x - prev.topRight.x, rawQuad.topRight.y - prev.topRight.y)
    val dBR = hypot(rawQuad.bottomRight.x - prev.bottomRight.x, rawQuad.bottomRight.y - prev.bottomRight.y)
    val dBL = hypot(rawQuad.bottomLeft.x - prev.bottomLeft.x, rawQuad.bottomLeft.y - prev.bottomLeft.y)
    val maxDelta = max(max(dTL, dTR), max(dBR, dBL))

    // Deadband threshold: if movement is under 1.5% of screen, do NOT move the coordinates at all
    // This completely suppresses camera sensor noise and finger tremors!
    if (maxDelta < 0.015f) {
        return prev
    }

    // Adaptive smoothing factor: smooth gliding for small moves, fast tracking for large shifts
    val alpha = if (maxDelta > 0.08f) 0.35f else 0.18f

    val smoothed = QuadCorners(
        topLeft = PointF(
            prev.topLeft.x + (rawQuad.topLeft.x - prev.topLeft.x) * alpha,
            prev.topLeft.y + (rawQuad.topLeft.y - prev.topLeft.y) * alpha
        ),
        topRight = PointF(
            prev.topRight.x + (rawQuad.topRight.x - prev.topRight.x) * alpha,
            prev.topRight.y + (rawQuad.topRight.y - prev.topRight.y) * alpha
        ),
        bottomRight = PointF(
            prev.bottomRight.x + (rawQuad.bottomRight.x - prev.bottomRight.x) * alpha,
            prev.bottomRight.y + (rawQuad.bottomRight.y - prev.bottomRight.y) * alpha
        ),
        bottomLeft = PointF(
            prev.bottomLeft.x + (rawQuad.bottomLeft.x - prev.bottomLeft.x) * alpha,
            prev.bottomLeft.y + (rawQuad.bottomLeft.y - prev.bottomLeft.y) * alpha
        )
    )

    lastQuadCorners = smoothed
    return smoothed
}
