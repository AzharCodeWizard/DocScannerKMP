import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CropScreen.kt', 'r') as f:
    content = f.read()

# I will just replace RenderedDocumentView with LocalImage wherever it is
content = re.sub(
    r"RenderedDocumentView\s*\(\s*modifier\s*=\s*Modifier\.fillMaxSize\(\)\.padding\(24\.dp\),\s*documentType\s*=\s*com\.lufick\.docscanner\.engine\.DocumentType\.RECEIPT\s*\)",
    r"com.lufick.docscanner.platform.LocalImage(path = uiState.imagePath, modifier = Modifier.fillMaxSize().padding(24.dp))",
    content
)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CropScreen.kt', 'w') as f:
    f.write(content)
