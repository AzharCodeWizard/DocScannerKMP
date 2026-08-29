# Fix PlatformCamera.android.kt
with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformCamera.android.kt', 'r') as f:
    content = f.read()

edge_fun = """
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
"""

if "fun analyzeDocumentEdges" not in content:
    content += "\n" + edge_fun

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformCamera.android.kt', 'w') as f:
    f.write(content)


# Fix CameraScreen.kt
with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'r') as f:
    screen_content = f.read()

# Add missing imports
if "import androidx.compose.runtime.LaunchedEffect" not in screen_content:
    screen_content = screen_content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect")

if "import androidx.compose.foundation.layout.widthIn" not in screen_content:
    screen_content = screen_content.replace("import androidx.compose.foundation.layout.width", "import androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.layout.widthIn")

# Ensure cameraHandler is defined before LaunchedEffect
screen_content = screen_content.replace("var cameraHandler by remember { mutableStateOf<PlatformCameraHandler?>(null) }", "")

old_func = """fun CameraScreen(
    viewModel: CameraViewModel,
    onClose: () -> Unit,
    onNavigateToCrop: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()"""

new_func = """fun CameraScreen(
    viewModel: CameraViewModel,
    onClose: () -> Unit,
    onNavigateToCrop: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var cameraHandler by remember { mutableStateOf<PlatformCameraHandler?>(null) }"""

screen_content = screen_content.replace(old_func, new_func)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CameraScreen.kt', 'w') as f:
    f.write(screen_content)
