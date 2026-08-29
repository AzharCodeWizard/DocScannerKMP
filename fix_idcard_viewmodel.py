import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/viewmodel/IdCardViewModel.kt', 'r') as f:
    content = f.read()

import_str = "import kotlinx.coroutines.flow.asStateFlow"
if "import com.lufick.docscanner.platform.PlatformImageProcessor" not in content:
    content = content.replace(import_str, import_str + "\nimport com.lufick.docscanner.platform.PlatformImageProcessor\nimport androidx.lifecycle.viewModelScope\nimport kotlinx.coroutines.launch")

old_class = "class IdCardViewModel : ViewModel() {"
new_class = "class IdCardViewModel(private val imageProcessor: PlatformImageProcessor) : ViewModel() {"
content = content.replace(old_class, new_class)

old_back_captured = """    fun onBackCaptured(path: String) {
        _uiState.value = _uiState.value.copy(
            backImagePath = path,
            currentSide = IdCardSide.PREVIEW
        )
    }"""

new_back_captured = """    fun onBackCaptured(path: String) {
        _uiState.value = _uiState.value.copy(
            backImagePath = path,
            currentSide = IdCardSide.PREVIEW
        )
        // Trigger stitch
        viewModelScope.launch {
            val front = _uiState.value.frontImagePath
            val back = path
            if (front != null) {
                val stitched = imageProcessor.stitchIdCard(front, back)
                _uiState.value = _uiState.value.copy(stitchedImagePath = stitched)
            }
        }
    }"""

content = content.replace(old_back_captured, new_back_captured)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/viewmodel/IdCardViewModel.kt', 'w') as f:
    f.write(content)
