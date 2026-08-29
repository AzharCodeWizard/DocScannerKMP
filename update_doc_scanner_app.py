with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/DocScannerApp.kt', 'r') as f:
    content = f.read()

# Replace val cropViewModel = remember { CropViewModel() }
old_vm_init = "val cropViewModel = remember { CropViewModel() }"
new_vm_init = "val cropViewModel = remember { CropViewModel(imageProcessor) }"
content = content.replace(old_vm_init, new_vm_init)

# Replace cropViewModel.setImage call
old_set_call = "cropViewModel.setImage(lastCaptured, template)"
new_set_call = """val detectedCorners = cameraViewModel.uiState.value.detectedQuad
                        cropViewModel.setImage(lastCaptured, template, initialCorners = detectedCorners)"""
content = content.replace(old_set_call, new_set_call)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/DocScannerApp.kt', 'w') as f:
    f.write(content)
