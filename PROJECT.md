# Project: DocScanner KMP Production Readiness

## Architecture
- **Multiplatform Framework**: Kotlin Multiplatform 2.0.21 with Compose Multiplatform 1.7.3
- **Targets**:
  - `composeApp`: Shared UI, ViewModels, business logic, math engines (`commonMain`), and platform bridges (`androidMain`, `iosMain`)
  - `androidApp`: Android application module, Application class, MainActivity, AndroidManifest
  - `iosApp`: SwiftUI iOS project shell hosting MainViewController
- **Core Processing Engines**:
  - `Homography.kt`: 8-DOF Direct Linear Transform perspective solver
  - `FilterEngine.kt`: 4x5 ColorMatrix GPU filter generator (Magic Color, B&W, Eco Print)
  - `OcrParser.kt`: Structured receipt/invoice regex & heuristic entity extractor
  - `QrCodeGenerator.kt`: Pure Kotlin QR matrix generator
  - `PlatformCamera`: CameraX image capture & live gradient edge detection
  - `PlatformPdfEngine`: Multi-page PDF renderer with AES encryption & watermarks
- **Telemetry & Diagnostics**:
  - `DocScannerAnalytics`: Multiplatform analytics interface with Android Firebase Analytics implementation & safe offline fallback
  - `DocScannerCrashlytics`: Multiplatform crash reporting interface with Android Firebase Crashlytics & offline logging

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Multi-Page Camera Scanning | CameraX capture with live edge gradient sampling, deadband smoothing, and auto-capture | M0 (Verified) | ORIGINAL_REQUEST §R1 |
| 2 | Homography Perspective Dewarp | 4-point loupe magnifier quad crop with Gaussian DLT solver | M0 (Verified) | ORIGINAL_REQUEST §R1 |
| 3 | GPU ColorMatrix Filters | Real-time Compose ColorMatrix filters (Magic Color 1/2, Sharp B&W, Eco Print, sliders) | M0 (Verified) | ORIGINAL_REQUEST §R1 |
| 4 | 2-in-1 ID Card Scanner | Dual-frame front & back guided capture stitched onto A4 canvas | M0 (Verified) | ORIGINAL_REQUEST §R1 |
| 5 | QR & Barcode Studio | Google Pay auto-zooming barcode scanner + multi-type QR matrix generator/vault | M0 (Verified) | ORIGINAL_REQUEST §R1 |
| 6 | ML Kit OCR & Entity Extractor | On-device text recognition with receipt/invoice entity heuristic parsing | M0 (Verified) | ORIGINAL_REQUEST §R1 |
| 7 | PDF Tools & E-Signature | PDF generation, AES password encryption, custom watermarking, vector signature pad | M0 (Verified) | ORIGINAL_REQUEST §R1 |
| 8 | Document Organizer & App Lock | Full-text search, categories, tags, and PIN/Biometric lock | M0 (Verified) | ORIGINAL_REQUEST §R1 |
| 9 | Firebase Build & Dependency Config | Google Services 4.4.2, Crashlytics 3.0.3, Firebase BoM 33.7.0 in Gradle | M1 | ORIGINAL_REQUEST §R2 |
| 10 | Template Credentials & Build Safety | Template `google-services.json` and `GoogleService-Info.plist` for clean offline builds | M1 | ORIGINAL_REQUEST §R2 |
| 11 | Safe Firebase Analytics & Telemetry | Multiplatform `DocScannerAnalytics` with 8 core event hooks and safe offline fallbacks | M1 | ORIGINAL_REQUEST §R2 |
| 12 | Safe Firebase Crashlytics & Error Logging | Multiplatform `DocScannerCrashlytics` non-fatal exception logging | M1 | ORIGINAL_REQUEST §R2 |
| 13 | About & Settings Screen Expansion | Dynamic App Version (1.0.0), Build Code (1), and release badge | M2 | ORIGINAL_REQUEST §R3 |
| 14 | Open Source Software Licenses Viewer | Interactive offline viewer for all open-source libraries | M2 | ORIGINAL_REQUEST §R3 |
| 15 | Offline Privacy Policy Viewer | In-app 100% on-device privacy guarantee legal viewer | M2 | ORIGINAL_REQUEST §R3 |
| 16 | Offline Terms of Service Viewer | In-app offline terms of service viewer | M2 | ORIGINAL_REQUEST §R3 |
| 17 | Support Contact & Feedback Trigger | Diagnostic email intent prefilling device model, OS, and app version | M2 | ORIGINAL_REQUEST §R3 |
| 18 | High-Res App Launcher Icon | `playstore_assets/icon_512x512.png` 512x512 modern icon | M3 | ORIGINAL_REQUEST §R4 |
| 19 | Play Store Feature Graphic | `playstore_assets/feature_graphic_1024x500.png` 1024x500 promotional banner | M3 | ORIGINAL_REQUEST §R4 |
| 20 | 8x Play Store Screenshot Mockups | `playstore_assets/screenshot_*.png` 1080x2400 high-res mockups for 8 core flows | M3 | ORIGINAL_REQUEST §R4 |
| 21 | Play Store Listing Copy | `playstore_assets/store_listing.md` with title, short desc, full desc, release notes | M3 | ORIGINAL_REQUEST §R4 |
| 22 | Multiplatform Unit Test Suite | Comprehensive tests for FilterEngine, Homography, OcrParser, QR Generator, Analytics | M4 | ORIGINAL_REQUEST §R5 |
| 23 | Build & Verification Gate | `./gradlew :composeApp:allTests` passes & `./gradlew :androidApp:assembleDebug` succeeds | M4 | ORIGINAL_REQUEST §R5 |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Firebase Analytics & Crashlytics Integration | Dependencies, template JSON/plist, multiplatform analytics/crashlytics adapters, event hooks | none | PLANNED |
| M2 | In-App About, Legal & Policy Pages | Version badge, OSS licenses, offline Privacy Policy & Terms of Service, support mailer | none | PLANNED |
| M3 | Google Play Store Release Asset Bundle | Python script generating `icon_512x512.png`, `feature_graphic_1024x500.png`, 8x `1080x2400` screenshots, and `store_listing.md` | none | PLANNED |
| M4 | E2E Testing, Test Suite & Final Build Verification | Unit tests in `commonTest`, verification of `:composeApp:allTests` and `:androidApp:assembleDebug` | M1, M2, M3 | PLANNED |

## Interface Contracts
### Telemetry Contracts
```kotlin
package com.lufick.docscanner.analytics

interface DocScannerAnalytics {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun logScreenView(screenName: String)
    fun logDocScanned(pageCount: Int, mode: String)
    fun logPdfExported(pageCount: Int, hasPassword: Boolean, hasWatermark: Boolean)
    fun logOcrPerformed(charCount: Int, entityCount: Int)
    fun logFilterApplied(filterName: String)
    fun logIdCardScanned()
    fun logQrAction(actionType: String, payloadType: String)
    fun logAppLockToggled(enabled: Boolean)
}

interface DocScannerCrashlytics {
    fun logException(throwable: Throwable, context: String? = null)
    fun setCustomKey(key: String, value: String)
    fun setUserId(userId: String)
}
```

### Settings & Legal Contracts
```kotlin
package com.lufick.docscanner.ui.legal

data class OpenSourceLicense(val name: String, val author: String, val licenseType: String, val url: String)
object LegalConstants {
    val APP_VERSION = "1.0.0"
    val BUILD_CODE = 1
    val PRIVACY_POLICY_TEXT: String = ...
    val TERMS_OF_SERVICE_TEXT: String = ...
    val OPEN_SOURCE_LICENSES: List<OpenSourceLicense> = ...
}
```

## Code Layout
- `composeApp/src/commonMain/kotlin/com/lufick/docscanner/`
  - `analytics/`: `DocScannerAnalytics.kt`, `DocScannerCrashlytics.kt`
  - `engine/`: `Homography.kt`, `FilterEngine.kt`, `OcrParser.kt`
  - `ui/legal/`: `LegalConstants.kt`, `LegalDialogs.kt`
  - `ui/screens/`: `SettingsScreen.kt`, `HomeScreen.kt`, `CameraScannerScreen.kt`, etc.
  - `util/`: `QrCodeGenerator.kt`
- `composeApp/src/androidMain/kotlin/com/lufick/docscanner/`
  - `analytics/`: `PlatformAnalytics.android.kt`, `PlatformCrashlytics.android.kt`
- `composeApp/src/iosMain/kotlin/com/lufick/docscanner/`
  - `analytics/`: `PlatformAnalytics.ios.kt`, `PlatformCrashlytics.ios.kt`
- `composeApp/src/commonTest/kotlin/com/lufick/docscanner/`
  - `engine/`: `FilterEngineTest.kt`, `HomographyTest.kt`, `OcrParserTest.kt`
  - `util/`: `QrCodeGeneratorTest.kt`
  - `analytics/`: `AnalyticsTest.kt`
- `androidApp/`
  - `google-services.json`
- `iosApp/iosApp/`
  - `GoogleService-Info.plist`
- `playstore_assets/`
  - `icon_512x512.png`
  - `feature_graphic_1024x500.png`
  - `screenshot_1_home_dashboard.png`
  - `screenshot_2_camera_laser.png`
  - `screenshot_3_dewarp_crop.png`
  - `screenshot_4_gpu_filters.png`
  - `screenshot_5_pdf_tools_signature.png`
  - `screenshot_6_id_card_stitch.png`
  - `screenshot_7_qr_barcode_studio.png`
  - `screenshot_8_ai_ocr_extractor.png`
  - `store_listing.md`
