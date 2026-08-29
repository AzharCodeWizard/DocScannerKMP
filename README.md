# 📱 DocScanner KMP — Lufick-Grade Document Scanner for Android & iOS

**DocScanner KMP** is a cross-platform mobile document scanner application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. It closely replicates the feature set, performance, and workflow of **Lufick Document Scanner** (CamScanner alternative with 10M+ downloads on Google Play).

---

## 🚀 Key Feature Matrix

1. **Camera & Live Edge Detection**:
   - **Document Mode**: Real-time laser scanning line with automated quad boundary tracking.
   - **ID Card Mode**: Guided dual-frame scanning (Front & Back) with automated single-page A4 stitching.
   - **Book Mode**: Dual-facing page scan with automatic center-split.
   - **Batch Mode**: High-speed burst document scanning with live page buffer counter.
   - **QR & Barcode Scanner**: Instant on-device decoding.

2. **Perspective Warp & Quad Crop Engine**:
   - 4-point draggable corner pins with real-time projective homography matrix calculation.
   - Touch loupe magnifier for sub-pixel alignment accuracy.
   - 1-tap Auto-Fit, Full-Page, and 90° rotation.

3. **Neural Enhancement & Filter Engine**:
   - **Magic Color 1 & 2** (Lufick Signature): High-contrast color boost with pure paper white leveling.
   - **Sharp B&W**: High-contrast binarization for clean, legible receipts and legal text.
   - **Smooth Grayscale**: Noise suppression with preserved photo textures.
   - **Live Contrast / Brightness Tuning**: Custom sliders with instant visual updates.

4. **Multi-Page Assembly & Document Detail**:
   - Reorder pages via drag-and-drop.
   - Rotate individual pages or all pages.
   - Append new pages anytime from camera or gallery.

5. **AI OCR & Entity Extraction**:
   - On-device text recognition with bounding line mapping.
   - Structured metadata extractor: **Merchant Name**, **Date**, **Total Amount**, **Tax / GST**, **Invoice #**.
   - 1-click clipboard copy, `.txt` export, Word export, and searchable PDF embedding.

6. **PDF Creation, Security & E-Signature**:
   - **Page Standards**: A4, US Letter, US Legal, A3, A5, Business Card.
   - **PDF Compression**: High (300 DPI), Balanced (200 DPI), Small (150 DPI) with live size calculator.
   - **AES Password Protection**: Encrypt generated PDFs with user passwords.
   - **Custom Watermarks**: Configurable text, opacity, font size, and rotation angle.
   - **Vector E-Signature**: Interactive signature drawing pad with transparent background embedding.

7. **Document Management & Organizer**:
   - Categorized Folders (Receipts, Legal, ID Cards, Personal, Tax).
   - Smart Color Tags, Favorites, and OCR full-text search.

---

## 🛠️ Architecture & Tech Stack

```
DocScannerKMP/
├── composeApp/                     # Shared Multiplatform Module
│   ├── commonMain/
│   │   ├── kotlin/com/lufick/docscanner/
│   │   │   ├── core/theme/         # Material 3 Theme (Lufick Emerald & Obsidian)
│   │   │   ├── model/              # Domain entities (Document, Page, OcrResult, PdfConfig)
│   │   │   ├── engine/             # Homography math, FilterEngine, OCR entity parser
│   │   │   ├── repository/         # DocumentRepository & InMemoryDocumentRepository
│   │   │   ├── platform/           # Expect declarations (Camera, PDF, OCR, ImageProcessor, Share)
│   │   │   ├── viewmodel/          # MVI ViewModels (Home, Camera, Crop, Filter, Detail, OCR, PdfTools, IdCard)
│   │   │   └── ui/screens/         # 8 Full Compose Multiplatform screens
│   ├── androidMain/                # Android Actuals (CameraX, ML Kit, Android PdfDocument)
│   └── iosMain/                    # iOS Actuals (AVFoundation, Vision OCR, PDFKit, UIKit bridge)
├── androidApp/                     # Android Application Entry Point (MainActivity)
└── iosApp/                         # iOS SwiftUI Application Entry Point (iOSApp.swift)
```

---

## 🏃 Getting Started

### Prerequisites
- **JDK 21** or later
- **Android Studio Ladybug / Meerkat** (or IntelliJ IDEA with KMP plugin)
- **Xcode 15+** (for building the iOS app)

### Running on Android
Open the root `DocScannerKMP` directory in Android Studio, select the `androidApp` run configuration, and press **Run**.

Alternatively, via command line:
```bash
./gradlew :androidApp:installDebug
```

### Running on iOS
Open `iosApp/iosApp.xcodeproj` in Xcode, choose an iOS Simulator or connected iPhone, and press **Cmd + R**.

### Running Unit Tests
```bash
./gradlew :composeApp:test
```

---

## 📄 License
MIT License. Built for production cross-platform scanning workflows.
