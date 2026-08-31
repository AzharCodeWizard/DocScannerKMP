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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private const val TAG = "DocScannerCamera"

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
                                        if (isQrScanMode) {
                                            analyzeQrAndAutoZoom(imageProxy, barcodeScanner, cameraInstance, onQrDetected)
                                        } else {
                                            val detectedQuad = analyzeDocumentEdges(imageProxy)
                                            onEdgeDetected(detectedQuad)
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
                val maxDim = Math.max(qrWidth, qrHeight)
                val ratio = (maxDim / frameW.toFloat()).coerceIn(0.01f, 1.0f)

                // Google Pay Auto-Zoom Algorithm:
                // If QR code is far away / small (< 32% of frame width), auto-zoom in smoothly!
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

private fun analyzeDocumentEdges(imageProxy: androidx.camera.core.ImageProxy): QuadCorners {
    val plane = imageProxy.planes[0]
    val buffer = plane.buffer
    val width = imageProxy.width
    val height = imageProxy.height
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride

    val defaultMinX = 0.08f
    val defaultMaxX = 0.92f
    val defaultMinY = 0.12f
    val defaultMaxY = 0.62f

    val sampleW = 24
    val sampleH = 24
    val stepX = (width / sampleW).coerceAtLeast(1)
    val stepY = (height / sampleH).coerceAtLeast(1)

    var sumDiff = 0
    var edgeMinX = defaultMinX
    var edgeMaxX = defaultMaxX
    var edgeMinY = defaultMinY
    var edgeMaxY = defaultMaxY

    val grid = Array(sampleH) { IntArray(sampleW) }
    for (y in 0 until sampleH) {
        val srcY = (y * stepY).coerceIn(0, height - 1)
        for (x in 0 until sampleW) {
            val srcX = (x * stepX).coerceIn(0, width - 1)
            val index = srcY * rowStride + srcX * pixelStride
            if (index < buffer.limit()) {
                grid[y][x] = buffer.get(index).toInt() and 0xFF
            }
        }
    }

    var maxHGrad = 0
    for (y in 2 until sampleH - 2) {
        var rowGrad = 0
        for (x in 2 until sampleW - 2) {
            val diff = Math.abs(grid[y + 1][x] - grid[y - 1][x])
            rowGrad += diff
        }
        if (rowGrad > maxHGrad) {
            maxHGrad = rowGrad
        }
        sumDiff += rowGrad
    }

    if (sumDiff > 3500) {
        val contrastFactor = (sumDiff / 80000f).coerceIn(0.0f, 1.0f)
        val insetX = 0.03f * contrastFactor
        val insetY = 0.04f * contrastFactor
        edgeMinX = (defaultMinX + insetX).coerceIn(0.06f, 0.12f)
        edgeMaxX = (defaultMaxX - insetX).coerceIn(0.88f, 0.94f)
        edgeMinY = (defaultMinY + insetY).coerceIn(0.11f, 0.15f)
        edgeMaxY = (defaultMaxY - insetY).coerceIn(0.59f, 0.63f)
    }

    return QuadCorners(
        topLeft = PointF(edgeMinX, edgeMinY),
        topRight = PointF(edgeMaxX, edgeMinY),
        bottomRight = PointF(edgeMaxX, edgeMaxY),
        bottomLeft = PointF(edgeMinX, edgeMaxY)
    )
}
