import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CropScreen.kt', 'r') as f:
    content = f.read()

bad_str = """                com.lufick.docscanner.platform.LocalImage(path = uiState.imagePath, modifier = Modifier.fillMaxSize().padding(24.dp)),
                    templateType = uiState.templateType,
                    rotationDegrees = uiState.rotationDegrees
                )"""

good_str = """                com.lufick.docscanner.platform.LocalImage(path = uiState.imagePath, modifier = Modifier.fillMaxSize().padding(24.dp))"""

content = content.replace(bad_str, good_str)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CropScreen.kt', 'w') as f:
    f.write(content)
