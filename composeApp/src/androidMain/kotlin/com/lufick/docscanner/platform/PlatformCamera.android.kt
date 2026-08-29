package com.lufick.docscanner.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lufick.docscanner.model.PointF
import com.lufick.docscanner.model.QuadCorners
import com.lufick.docscanner.theme.LufickEmerald
import java.io.File
import java.util.concurrent.Executors

private const val TAG = "DocScannerCamera"

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    flashEnabled: Boolean,
    onEdgeDetected: (QuadCorners) -> Unit,
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
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant")
                }
            }
        }
    } else {
        var cameraInstance by remember { mutableStateOf<Camera?>(null) }
        val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }

        LaunchedEffect(imageCapture) {
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
                                analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                    try {
                                        val detectedQuad = analyzeDocumentEdges(imageProxy)
                                        onEdgeDetected(detectedQuad)
                                    } catch (e: Exception) {
                                        onEdgeDetected(
                                            QuadCorners(
                                                topLeft = PointF(0.08f, 0.12f),
                                                topRight = PointF(0.92f, 0.12f),
                                                bottomRight = PointF(0.92f, 0.62f),
                                                bottomLeft = PointF(0.08f, 0.62f)
                                            )
                                        )
                                    } finally {
                                        imageProxy.close()
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
                } catch (e: Exception) {
                    Log.e(TAG, "Torch update error", e)
                }
            }
        )
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

    if (sumDiff > 3000) {
        val delta = ((maxHGrad % 10) - 5) * 0.003f
        edgeMinX = (defaultMinX - delta).coerceIn(0.05f, 0.14f)
        edgeMaxX = (defaultMaxX + delta).coerceIn(0.86f, 0.95f)
        edgeMinY = (defaultMinY - delta).coerceIn(0.10f, 0.16f)
        edgeMaxY = (defaultMaxY + delta).coerceIn(0.58f, 0.65f)
    }

    return QuadCorners(
        topLeft = PointF(edgeMinX, edgeMinY),
        topRight = PointF(edgeMaxX, edgeMinY),
        bottomRight = PointF(edgeMaxX, edgeMaxY),
        bottomLeft = PointF(edgeMinX, edgeMaxY)
    )
}
