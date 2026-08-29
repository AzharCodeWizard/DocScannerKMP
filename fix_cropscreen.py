import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CropScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.lufick.docscanner.engine.RenderedDocumentView", "import com.lufick.docscanner.platform.LocalImage")

old_render = """                // Synthetic Rendered Document (Under the Crop layer)
                RenderedDocumentView(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    documentType = com.lufick.docscanner.engine.DocumentType.RECEIPT
                )"""

new_render = """                // Actual Captured Image
                LocalImage(
                    path = uiState.imagePath,
                    modifier = Modifier.fillMaxSize()
                )"""

content = content.replace(old_render, new_render)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CropScreen.kt', 'w') as f:
    f.write(content)
