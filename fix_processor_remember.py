import re

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.kt', 'r') as f:
    content = f.read()

content += "\n\nimport androidx.compose.runtime.Composable\n@Composable\nexpect fun rememberPlatformImageProcessor(): PlatformImageProcessor\n"
with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.kt', 'w') as f:
    f.write(content)

with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'r') as f:
    android_content = f.read()

android_content += "\n\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.remember\nimport androidx.compose.ui.platform.LocalContext\n\n@Composable\nactual fun rememberPlatformImageProcessor(): PlatformImageProcessor {\n    val context = LocalContext.current\n    return remember { PlatformImageProcessor(context) }\n}\n"
with open('composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.android.kt', 'w') as f:
    f.write(android_content)

with open('composeApp/src/iosMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.ios.kt', 'r') as f:
    ios_content = f.read()

ios_content += "\n\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.remember\n\n@Composable\nactual fun rememberPlatformImageProcessor(): PlatformImageProcessor {\n    return remember { PlatformImageProcessor() }\n}\n"
with open('composeApp/src/iosMain/kotlin/com/lufick/docscanner/platform/PlatformImageProcessor.ios.kt', 'w') as f:
    f.write(ios_content)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/DocScannerApp.kt', 'r') as f:
    app_content = f.read()

app_content = app_content.replace("import com.lufick.docscanner.viewmodel.IdCardViewModel", "import com.lufick.docscanner.viewmodel.IdCardViewModel\nimport com.lufick.docscanner.platform.rememberPlatformImageProcessor")
app_content = app_content.replace("val idCardViewModel = remember { IdCardViewModel(imageProcessor) }", "val imageProcessor = rememberPlatformImageProcessor()\n    val idCardViewModel = remember { IdCardViewModel(imageProcessor) }")

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/DocScannerApp.kt', 'w') as f:
    f.write(app_content)
