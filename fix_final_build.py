# Fix PlatformImageProcessor.android.kt
with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'r') as f:
    content = f.read()

if "import com.lufick.docscanner.model.PointF" not in content:
    content = content.replace("import com.lufick.docscanner.model.QuadCorners", "import com.lufick.docscanner.model.PointF\nimport com.lufick.docscanner.model.QuadCorners")

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'w') as f:
    f.write(content)

# Fix DocScannerApp.kt
with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/DocScannerApp.kt', 'r') as f:
    app_content = f.read()

old_declarations = """    val homeViewModel = remember { HomeViewModel(repository) }
    val cameraViewModel = remember { CameraViewModel() }
    val cropViewModel = remember { CropViewModel(imageProcessor) }
    val filterViewModel = remember { FilterViewModel() }
    val detailViewModel = remember { DocumentDetailViewModel(repository) }
    val ocrViewModel = remember { OcrViewModel() }
    val pdfToolsViewModel = remember { PdfToolsViewModel() }
    val imageProcessor = rememberPlatformImageProcessor()
    val idCardViewModel = remember { IdCardViewModel(imageProcessor) }"""

new_declarations = """    val imageProcessor = rememberPlatformImageProcessor()
    val homeViewModel = remember { HomeViewModel(repository) }
    val cameraViewModel = remember { CameraViewModel() }
    val cropViewModel = remember { CropViewModel(imageProcessor) }
    val filterViewModel = remember { FilterViewModel() }
    val detailViewModel = remember { DocumentDetailViewModel(repository) }
    val ocrViewModel = remember { OcrViewModel() }
    val pdfToolsViewModel = remember { PdfToolsViewModel() }
    val idCardViewModel = remember { IdCardViewModel(imageProcessor) }"""

app_content = app_content.replace(old_declarations, new_declarations)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/DocScannerApp.kt', 'w') as f:
    f.write(app_content)
