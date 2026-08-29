with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/viewmodel/CropViewModel.kt', 'r') as f:
    content = f.read()

# Add imports for imageProcessor and coroutines
if "import com.lufick.docscanner.platform.PlatformImageProcessor" not in content:
    content = content.replace("import com.lufick.docscanner.model.QuadCorners", "import com.lufick.docscanner.model.QuadCorners\nimport com.lufick.docscanner.platform.PlatformImageProcessor\nimport androidx.lifecycle.viewModelScope\nimport kotlinx.coroutines.launch")

# Update class constructor
content = content.replace("class CropViewModel : ViewModel() {", "class CropViewModel(private val imageProcessor: PlatformImageProcessor? = null) : ViewModel() {")

# Update setImage
old_set_image = """    fun setImage(path: String, template: DocumentTemplateType = DocumentTemplateType.RECEIPT) {
        _uiState.value = _uiState.value.copy(
            imagePath = path,
            templateType = template,
            rotationDegrees = 0,
            corners = QuadCorners(
                topLeft = PointF(0.06f, 0.08f),
                topRight = PointF(0.94f, 0.08f),
                bottomRight = PointF(0.94f, 0.92f),
                bottomLeft = PointF(0.06f, 0.92f)
            )
        )
    }"""

new_set_image = """    fun setImage(
        path: String,
        template: DocumentTemplateType = DocumentTemplateType.RECEIPT,
        initialCorners: QuadCorners? = null
    ) {
        val defaultQuad = initialCorners ?: QuadCorners(
            topLeft = PointF(0.06f, 0.08f),
            topRight = PointF(0.94f, 0.08f),
            bottomRight = PointF(0.94f, 0.92f),
            bottomLeft = PointF(0.06f, 0.92f)
        )
        _uiState.value = _uiState.value.copy(
            imagePath = path,
            templateType = template,
            rotationDegrees = 0,
            corners = defaultQuad
        )

        // If no initial corners provided from camera, run auto-detection on the captured image
        if (initialCorners == null && imageProcessor != null && path.isNotEmpty()) {
            autoDetect()
        }
    }"""

content = content.replace(old_set_image, new_set_image)

# Update autoDetect to run real detection
old_auto_detect = """    fun autoDetect() {
        _uiState.value = _uiState.value.copy(
            corners = QuadCorners(
                topLeft = PointF(0.08f, 0.10f),
                topRight = PointF(0.92f, 0.10f),
                bottomRight = PointF(0.90f, 0.90f),
                bottomLeft = PointF(0.10f, 0.90f)
            )
        )
    }"""

new_auto_detect = """    fun autoDetect() {
        val currentPath = _uiState.value.imagePath
        if (imageProcessor != null && currentPath.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val detected = imageProcessor.detectDocumentCorners(currentPath)
                    _uiState.value = _uiState.value.copy(corners = detected)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        corners = QuadCorners(
                            topLeft = PointF(0.08f, 0.08f),
                            topRight = PointF(0.92f, 0.08f),
                            bottomRight = PointF(0.92f, 0.92f),
                            bottomLeft = PointF(0.08f, 0.92f)
                        )
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                corners = QuadCorners(
                    topLeft = PointF(0.08f, 0.08f),
                    topRight = PointF(0.92f, 0.08f),
                    bottomRight = PointF(0.92f, 0.92f),
                    bottomLeft = PointF(0.08f, 0.92f)
                )
            )
        }
    }"""

content = content.replace(old_auto_detect, new_auto_detect)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/viewmodel/CropViewModel.kt', 'w') as f:
    f.write(content)
