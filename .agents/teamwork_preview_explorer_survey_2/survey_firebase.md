# Firebase Analytics & Crashlytics Integration Survey (R2)
**Project**: DocScanner KMP (Kotlin Multiplatform: Android & iOS)  
**Author**: Firebase Integration Explorer  
**Date**: 2026-08-31  
**Status**: Completed & Ready for Implementation  

---

## 1. Executive Summary

DocScanner KMP requires a production-grade integration of **Firebase Analytics** and **Firebase Crashlytics** to monitor document scanning workflows, PDF tool operations, OCR text extraction, GPU color filtering, and capture non-fatal exceptions without sacrificing build stability or runtime performance.

### Key Architectural Decisions:
1. **Multiplatform Clean Architecture (Option C)**: Rather than coupling UI and ViewModels directly to raw SDK singletons, introduce strongly-typed domain interfaces (`DocScannerAnalytics` and `DocScannerCrashlytics`) in `commonMain`, backed by platform `expect`/`actual` adapters (`PlatformAnalytics` and `PlatformCrashlytics`).
2. **Defensive Safe Initialization**: Guard all Firebase initialization and dispatch calls in `androidMain` and `iosMain` with runtime checks (`FirebaseApp.getApps().isNotEmpty()`, structured `try-catch` blocks). The app will operate 100% crash-free even if `google-services.json` or `GoogleService-Info.plist` contains placeholder dummy credentials or is missing.
3. **Build-Time Resilience**: Provide valid template/mock configuration files (`androidApp/google-services.json` and `iosApp/iosApp/GoogleService-Info.plist`) with bundle ID `com.lufick.docscanner` so that CI/CD, local developer machines, and `./gradlew :composeApp:allTests :androidApp:assembleDebug` build cleanly out of the box with zero configuration friction.
4. **Zero CocoaPods Lock-In**: iOS builds use standard KMP static framework export (`baseName = "composeApp"`) without mandating the CocoaPods Gradle plugin. `iosMain` safely routes telemetry to Apple's Unified Logging (`os_log` / `NSLog`) and forwards to the native Firebase iOS SDK when linked in Xcode.
5. **Offline Operation**: Firebase Analytics and Crashlytics inherently queue events on-device in SQLite. Our wrapper ensures full offline resilience and maintains an in-memory/console debug stream during development.

---

## 2. Current Project Status & Build Configuration Audit

### 2.1 Baseline Health
- `./gradlew :composeApp:allTests`: **PASSED** (0 failures).
- `./gradlew :androidApp:assembleDebug`: **PASSED** (107 tasks up-to-date, build time ~1s).

### 2.2 Existing Dependencies Audit
- **`gradle/libs.versions.toml`**: Currently contains Kotlin `2.0.21`, AGP `8.13.2`, Compose Multiplatform `1.7.3`, Coroutines `1.9.0`, Koin `4.0.0`, CameraX `1.3.4`, and ML Kit (`play-services-mlkit-text-recognition:16.0.0`, `play-services-mlkit-barcode-scanning:18.3.0`).
- **Direct Firebase Dependencies**: **None currently exist**. (Transitive Firebase ComponentRegistrars are brought in by ML Kit).
- **Gradle Plugins**: Root `build.gradle.kts` and `androidApp/build.gradle.kts` currently lack `com.google.gms.google-services` and `com.google.firebase.crashlytics`.
- **Configuration Files**: No `google-services.json` exists in `androidApp/` and no `GoogleService-Info.plist` exists in `iosApp/iosApp/`.

---

## 3. Comparative Analysis: Firebase Integration Approaches

| Evaluation Criteria | Option A: GitLive Firebase KMP (`dev.gitlive:firebase-*`) | Option B: Official Google Native SDKs (Expect/Actual) | Option C: Clean Multiplatform Architecture (Domain Contract + Expect/Actual Safe Adapters) |
|---|---|---|---|
| **Architecture** | Direct dependency on 3rd-party KMP wrapper | Low-level expect/actual classes matching Google APIs | High-level domain contracts (`DocScannerAnalytics`, `DocScannerCrashlytics`) |
| **Android Implementation** | Wraps Firebase Android SDK | Uses Official Firebase BoM | Uses Official Firebase BoM with defensive wrapper |
| **iOS Implementation** | Requires CocoaPods Gradle plugin & cinterop klibs | Requires Swift/Obj-C bridge | Pure KMP actuals + Apple Unified Log + Swift bridge |
| **Build Stability** | Fragile with Kotlin 2.0.21 without CocoaPods | Good | **Excellent** (0 external Gradle plugin friction on iOS) |
| **Safe Fallback & Offline** | Hard to intercept initialization failures | Manual checks | **Built-in** (`SafeFirebaseAnalytics` decorator & fallback) |
| **Event Type Safety** | Untyped String key-value maps | Untyped maps | **Fully typed** domain methods (`trackPdfExported`, etc.) |
| **Unit Testability** | Hard (requires mocking static singletons) | Moderate | **Trivial** (`FakeDocScannerAnalytics` in `commonTest`) |

### Recommendation: **Option C (Clean Multiplatform Architecture)**
Option C provides the exact balance of native Google Firebase power on Android, seamless build safety on iOS, typed event definitions for DocScanner features, and 100% crash protection.

---

## 4. Safe Firebase Initialization & Offline Resilience

### 4.1 Build-Time Safety: Handling Missing Configuration Files
When the Google Services Gradle plugin (`com.google.gms.google-services`) is applied to `androidApp`, Gradle mandates that `google-services.json` exists. If missing, `./gradlew assembleDebug` immediately aborts.

**Solution**:
1. Place a valid template `google-services.json` in `/Users/azhar/Documents/Projects/DocScannerKMP/androidApp/google-services.json` matching the package name `com.lufick.docscanner`:
```json
{
  "project_info": {
    "project_number": "123456789012",
    "project_id": "docscanner-kmp",
    "storage_bucket": "docscanner-kmp.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789012:android:abcdef1234567890abcdef",
        "android_client_info": {
          "package_name": "com.lufick.docscanner"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "AIzaSyDummyKeyForBuildVerification1234567"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
```
2. Place a valid template `GoogleService-Info.plist` in `/Users/azhar/Documents/Projects/DocScannerKMP/iosApp/iosApp/GoogleService-Info.plist` with `BUNDLE_ID` set to `com.lufick.docscanner`.

### 4.2 Runtime Safety: Defensive Initialization
On Android, `FirebaseInitProvider` starts Firebase automatically. However, if credentials are dummy or Firebase fails to initialize, direct calls to `FirebaseAnalytics.getInstance(context)` or `FirebaseCrashlytics.getInstance()` can throw `IllegalStateException`.

**Defensive Wrapper Pattern (`PlatformAnalytics.android.kt` & `PlatformCrashlytics.android.kt`)**:
```kotlin
class AndroidFirebaseAnalytics(private val context: Context) : DocScannerAnalytics {
    private val firebaseAnalytics: FirebaseAnalytics? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAnalytics.getInstance(context)
            } else {
                null
            }
        } catch (e: Throwable) {
            null
        }
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        try {
            val bundle = Bundle().apply {
                params.forEach { (k, v) ->
                    when (v) {
                        is String -> putString(k, v)
                        is Int -> putInt(k, v)
                        is Long -> putLong(k, v)
                        is Double -> putDouble(k, v)
                        is Float -> putDouble(k, v.toDouble())
                        is Boolean -> putBoolean(k, v)
                        else -> putString(k, v.toString())
                    }
                }
            }
            firebaseAnalytics?.logEvent(name, bundle)
        } catch (e: Throwable) {
            // Log fallback to console/logcat without crashing
        }
    }
}
```

---

## 5. Firebase Analytics Event Taxonomy & Specification

### 5.1 Domain Interface: `DocScannerAnalytics` (`commonMain`)

```kotlin
package com.lufick.docscanner.analytics

import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.model.PdfConfig
import com.lufick.docscanner.model.ScanMode

interface DocScannerAnalytics {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
    fun logScreenView(screenName: String)

    // Domain-specific typed event helpers:
    fun trackDocumentScanned(pageCount: Int, scanMode: ScanMode, source: String = "camera")
    fun trackPdfExported(pageCount: Int, config: PdfConfig, estimatedSizeKb: Int)
    fun trackOcrPerformed(charCount: Int, wordCount: Int, language: String, entitiesCount: Int)
    fun trackFilterApplied(filterType: FilterType, brightness: Float, contrast: Float, saturation: Float)
    fun trackIdCardScanned(side: String, completedBothSides: Boolean)
    fun trackQrAction(actionType: String, qrType: String)
    fun trackAppLockToggled(enabled: Boolean, biometric: Boolean)
}
```

### 5.2 Event Dictionary

| Event Name (`name`) | Parameter Key | Data Type | Example Value | Description |
|---|---|---|---|---|
| **`doc_scanned`** | `page_count`<br>`scan_mode`<br>`source`<br>`auto_crop_used` | Int<br>String<br>String<br>Boolean | `3`<br>`"BATCH"`<br>`"camera"`<br>`true` | Logged when a scan batch or single page capture is completed and saved |
| **`pdf_exported`** | `page_count`<br>`file_size_kb`<br>`has_password`<br>`has_watermark`<br>`has_signature`<br>`page_size`<br>`quality`<br>`page_numbers` | Int<br>Int<br>Boolean<br>Boolean<br>Boolean<br>String<br>String<br>Boolean | `5`<br>`420`<br>`true`<br>`true`<br>`false`<br>`"A4"`<br>`"BALANCED"`<br>`true` | Logged upon exporting/saving a PDF document with custom configurations |
| **`ocr_performed`** | `char_count`<br>`word_count`<br>`detected_language`<br>`entities_count`<br>`search_used` | Int<br>Int<br>String<br>Int<br>Boolean | `1420`<br>`245`<br>`"en"`<br>`4`<br>`true` | Logged when ML Kit on-device OCR extracts text and structured entities |
| **`filter_applied`** | `filter_type`<br>`brightness`<br>`contrast`<br>`saturation`<br>`rotation_angle` | String<br>Float<br>Float<br>Float<br>Int | `"MAGIC_COLOR_1"`<br>`1.05`<br>`1.25`<br>`1.10`<br>`90` | Logged when a GPU ColorMatrix preset filter or manual adjustment is committed |
| **`id_card_scanned`** | `side`<br>`is_stitched`<br>`layout` | String<br>Boolean<br>String | `"back"`<br>`true`<br>`"single_page_dual"` | Logged during 2-in-1 ID card front/back capture and stitching |
| **`qr_studio_action`** | `action_type`<br>`qr_type`<br>`has_payload` | String<br>String<br>Boolean | `"generate"`<br>`"WIFI"`<br>`true` | Logged during QR/barcode scanning, generation, or vault save |
| **`app_lock_toggled`** | `is_enabled`<br>`biometric_enabled` | Boolean<br>Boolean | `true`<br>`true` | Logged when user modifies security PIN or biometric app lock |
| **`screen_view`** | `screen_name` | String | `"HomeScreen"`, `"PdfToolsScreen"` | Logged on composable screen navigation transitions |

---

## 6. Firebase Crashlytics & Non-Fatal Exception Logging Specification

### 6.1 Domain Interface: `DocScannerCrashlytics` (`commonMain`)

```kotlin
package com.lufick.docscanner.analytics

interface DocScannerCrashlytics {
    fun recordException(
        throwable: Throwable,
        message: String? = null,
        customAttributes: Map<String, String> = emptyMap()
    )
    fun log(message: String)
    fun setUserId(userId: String)
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
    fun setCustomKey(key: String, value: Int)
}
```

### 6.2 Key Codebase Integration Points for Non-Fatal Capture

1. **`PlatformImageProcessor` (Android & iOS)**:
   - *Failure Mode*: Matrix singular inversion during Homography perspective dewarp, Out-of-Memory during large bitmap scaling.
   - *Action*: Catch `Throwable`, record exception with attributes: `mapOf("operation" to "perspective_warp", "image_path" to sourceImagePath)`.
2. **`PlatformPdfEngine` (Android & iOS)**:
   - *Failure Mode*: PDF canvas rendering failure, file I/O permissions error, AES encryption failure.
   - *Action*: Catch `IOException`, record exception with attributes: `mapOf("document_title" to documentTitle, "page_count" to pages.size.toString())`.
3. **`PlatformOcrEngine` (Android & iOS)**:
   - *Failure Mode*: ML Kit TextRecognizer model failure, unreadable image buffer.
   - *Action*: Catch exception, record non-fatal with attributes: `mapOf("engine" to "mlkit_ocr")`.
4. **`PlatformCamera` (Android & iOS)**:
   - *Failure Mode*: CameraX binding exception, camera hardware disconnected, capture timeout.
   - *Action*: Catch `Exception`, record non-fatal with attributes: `mapOf("camera_mode" to mode.name)`.
5. **`InMemoryDocumentRepository` / File Storage**:
   - *Failure Mode*: JSON serialization or corrupted file cache.
   - *Action*: Catch `SerializationException`, record non-fatal.

---

## 7. Multiplatform Expect/Actual Architecture Structure

### 7.1 Target File Layout

```
composeApp/src/
├── commonMain/kotlin/com/lufick/docscanner/
│   └── analytics/
│       ├── DocScannerAnalytics.kt         // Main analytics interface & default methods
│       ├── DocScannerCrashlytics.kt       // Crashlytics interface
│       ├── AnalyticsEvents.kt             // Event names & parameter constants
│       ├── SafeDocScannerAnalytics.kt     // Exception-safe forwarding decorator
│       ├── NoOpDocScannerAnalytics.kt     // Offline / mock implementation
│       └── PlatformAnalytics.kt           // expect fun rememberPlatformAnalytics(), rememberPlatformCrashlytics()
├── androidMain/kotlin/com/lufick/docscanner/
│   └── analytics/
│       ├── AndroidFirebaseAnalytics.kt    // Official Firebase BoM Android implementation
│       ├── AndroidFirebaseCrashlytics.kt  // Official Firebase Crashlytics Android implementation
│       └── PlatformAnalytics.android.kt   // actual composable remember functions
├── iosMain/kotlin/com/lufick/docscanner/
│   └── analytics/
│       ├── IosFirebaseAnalytics.kt        // Apple OSLog & optional Native Firebase bridge
│       ├── IosFirebaseCrashlytics.kt      // Apple OSLog & non-fatal recorder
│       └── PlatformAnalytics.ios.kt       // actual composable remember functions
└── commonTest/kotlin/com/lufick/docscanner/
    └── analytics/
        ├── FakeDocScannerAnalytics.kt     // In-memory test tracker for unit tests
        └── AnalyticsVerificationTest.kt   // Tests verifying event emission
```

---

## 8. Detailed Gradle Build Script Updates

### 8.1 `gradle/libs.versions.toml`
```toml
[versions]
# ... existing versions ...
google-services = "4.4.2"
firebase-crashlytics-gradle = "3.0.3"
firebase-bom = "33.7.0"

[libraries]
# ... existing libraries ...
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase-bom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics" }

[plugins]
# ... existing plugins ...
google-services = { id = "com.google.gms.google-services", version.ref = "google-services" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version.ref = "firebase-crashlytics-gradle" }
```

### 8.2 Root `build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
```

### 8.3 `androidApp/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

dependencies {
    implementation(project(":composeApp"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.android)
}
```

### 8.4 `composeApp/build.gradle.kts`
```kotlin
kotlin {
    // ...
    sourceSets {
        commonMain.dependencies {
            // ...
        }
        androidMain.dependencies {
            // ...
            implementation(platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
        }
    }
}
```

---

## 9. Implementation Roadmap & Verification Plan

1. **Step 1: Configuration & Version Catalog**
   - Update `gradle/libs.versions.toml`, root `build.gradle.kts`, `androidApp/build.gradle.kts`, and `composeApp/build.gradle.kts`.
   - Add template `androidApp/google-services.json` and `iosApp/iosApp/GoogleService-Info.plist`.
2. **Step 2: Multiplatform Analytics & Crashlytics Layer**
   - Create interfaces and events in `commonMain/kotlin/com/lufick/docscanner/analytics/`.
   - Create Android actuals in `androidMain/kotlin/com/lufick/docscanner/analytics/`.
   - Create iOS actuals in `iosMain/kotlin/com/lufick/docscanner/analytics/`.
3. **Step 3: Integration into DocScanner UI & ViewModels**
   - Provide `rememberPlatformAnalytics()` and `rememberPlatformCrashlytics()` in `DocScannerApp.kt`.
   - Hook event dispatching in `HomeScreen`, `CameraScreen`, `CropScreen`, `FilterScreen`, `PdfToolsScreen`, `OcrScreen`, `IdCardScannerScreen`, `QrStudioScreen`, `SettingsScreen`.
   - Hook non-fatal error logging into `PlatformImageProcessor`, `PlatformPdfEngine`, and `PlatformOcrEngine`.
4. **Step 4: Verification & Automated Testing**
   - Write unit tests in `composeApp/src/commonTest/kotlin/com/lufick/docscanner/analytics/AnalyticsVerificationTest.kt` verifying that all domain event methods correctly build and dispatch payload bundles.
   - Run `./gradlew :composeApp:allTests` (target: 0 failures).
   - Run `./gradlew :androidApp:assembleDebug` (target: clean APK compilation).
   - Verify zero runtime crashes when launched in airplane/offline mode.
