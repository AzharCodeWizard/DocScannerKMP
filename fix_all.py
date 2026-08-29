import re

# Fix PlatformImageProcessor.kt
with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.kt', 'r') as f:
    content = f.read()
if "import androidx.compose.runtime.Composable" in content and content.index("import androidx.compose.runtime.Composable") > 100:
    content = content.replace("import androidx.compose.runtime.Composable\n@Composable\nexpect fun rememberPlatformImageProcessor(): PlatformImageProcessor\n", "")
    content = content.replace("package com.lufick.docscanner.platform", "package com.lufick.docscanner.platform\n\nimport androidx.compose.runtime.Composable")
    content += "\n@Composable\nexpect fun rememberPlatformImageProcessor(): PlatformImageProcessor\n"
with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.kt', 'w') as f:
    f.write(content)

# Fix PlatformImageProcessor.android.kt
with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'r') as f:
    android_content = f.read()
if "import androidx.compose.runtime.Composable" in android_content and android_content.index("import androidx.compose.runtime.Composable") > 200:
    android_content = android_content.replace("\n\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.remember\nimport androidx.compose.ui.platform.LocalContext\n\n@Composable\nactual fun rememberPlatformImageProcessor(): PlatformImageProcessor {\n    val context = LocalContext.current\n    return remember { PlatformImageProcessor(context) }\n}\n", "")
    android_content = android_content.replace("package com.lufick.docscanner.platform", "package com.lufick.docscanner.platform\n\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.remember\nimport androidx.compose.ui.platform.LocalContext")
    android_content += "\n@Composable\nactual fun rememberPlatformImageProcessor(): PlatformImageProcessor {\n    val context = LocalContext.current\n    return remember { PlatformImageProcessor(context) }\n}\n"
with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'w') as f:
    f.write(android_content)

# Fix PlatformImageProcessor.ios.kt
with open('composeApp/src/iosMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.ios.kt', 'r') as f:
    ios_content = f.read()
if "import androidx.compose.runtime.Composable" in ios_content and ios_content.index("import androidx.compose.runtime.Composable") > 200:
    ios_content = ios_content.replace("\n\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.remember\n\n@Composable\nactual fun rememberPlatformImageProcessor(): PlatformImageProcessor {\n    return remember { PlatformImageProcessor() }\n}\n", "")
    ios_content = ios_content.replace("package com.lufick.docscanner.platform", "package com.lufick.docscanner.platform\n\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.remember")
    ios_content += "\n@Composable\nactual fun rememberPlatformImageProcessor(): PlatformImageProcessor {\n    return remember { PlatformImageProcessor() }\n}\n"
with open('composeApp/src/iosMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.ios.kt', 'w') as f:
    f.write(ios_content)

# Fix CropScreen
with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CropScreen.kt', 'r') as f:
    crop_content = f.read()

# Replace any lingering RenderedDocumentView(...) completely
crop_content = re.sub(
    r"RenderedDocumentView\s*\([^)]*\)",
    r"com.lufick.docscanner.platform.LocalImage(path = uiState.imagePath, modifier = Modifier.fillMaxSize().padding(24.dp))",
    crop_content
)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/CropScreen.kt', 'w') as f:
    f.write(crop_content)
