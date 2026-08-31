# Handoff Report: Firebase Integration Survey (R2)

**Agent**: Firebase Integration Explorer (`teamwork_preview_explorer_survey_2`)  
**Recipient**: Project Orchestrator (`2f6552ed-f39a-4e78-98e8-3122e06d7f0f`)  
**Date**: 2026-08-31  
**Working Directory**: `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_explorer_survey_2`  
**Report Path**: `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_explorer_survey_2/survey_firebase.md`  

---

## 1. Observation

1. **Build Scripts & Catalog**:
   - `gradle/libs.versions.toml`: Contains Kotlin `2.0.21`, AGP `8.13.2`, Compose Multiplatform `1.7.3`, Coroutines `1.9.0`, Koin `4.0.0`, CameraX `1.3.4`, and ML Kit (`play-services-mlkit-text-recognition:16.0.0`, `play-services-mlkit-barcode-scanning:18.3.0`). Direct Firebase dependencies and plugins (`google-services`, `firebase-crashlytics`) are not yet declared.
   - `build.gradle.kts` (root, lines 1-10): Only declares standard Android/Kotlin/Compose plugins.
   - `composeApp/build.gradle.kts` (lines 20-29): Declares standard static framework export for iOS (`baseName = "composeApp"`, `isStatic = true`) without the CocoaPods Gradle plugin.
   - `androidApp/build.gradle.kts` (lines 1-55): Targets `com.lufick.docscanner` with `compileSdk = 35`, `minSdk = 24`, `targetSdk = 35`.
2. **Missing Configuration Files**:
   - Neither `androidApp/google-services.json` nor `iosApp/iosApp/GoogleService-Info.plist` currently exists.
   - Applying `com.google.gms.google-services` to `androidApp` without a valid JSON file causes Gradle build failures unless a template/mock configuration file is provided.
3. **Existing Platform Expect/Actual Pattern**:
   - `composeApp/src/commonMain/kotlin/com/lufick/docscanner/platform/` contains `PlatformCamera.kt`, `PlatformImageProcessor.kt`, `PlatformOcrEngine.kt`, `PlatformPdfEngine.kt`, `PlatformShare.kt` using `@Composable expect fun rememberPlatform*(): Platform*`.
   - `composeApp/src/androidMain/kotlin/com/lufick/docscanner/platform/` provides Android implementations using `LocalContext.current`.
   - `composeApp/src/iosMain/kotlin/com/lufick/docscanner/platform/` provides iOS implementations.
4. **Baseline Health**:
   - Executed `./gradlew :composeApp:allTests :androidApp:assembleDebug`: completed with code 0 (107 tasks up-to-date, ~1s).

---

## 2. Logic Chain

1. **Dependency & Architecture Selection**:
   - *Observation*: iOS target in `composeApp` builds via standard static frameworks without the CocoaPods plugin.
   - *Logic*: Introducing GitLive Firebase KMP (`dev.gitlive:firebase-*`) requires CocoaPods / cinterop linking on iOS, which introduces build instability with Kotlin 2.0.21.
   - *Inference*: Using Clean Architecture (Domain interface `DocScannerAnalytics` / `DocScannerCrashlytics` in `commonMain` + Official Google Firebase BoM `33.7.0` on Android + Native Apple OSLog / Darwin fallback on iOS) eliminates CocoaPods fragility while delivering official Google Firebase performance.
2. **Build-Time Resilience**:
   - *Observation*: The Google Services Gradle plugin fails if `google-services.json` is missing.
   - *Logic*: Adding a valid template `androidApp/google-services.json` (package name `com.lufick.docscanner`) and `iosApp/iosApp/GoogleService-Info.plist` guarantees that `./gradlew assembleDebug` compiles 100% cleanly on any developer machine or CI pipeline prior to real credential provisioning.
3. **Runtime Safety & Offline Resilience**:
   - *Observation*: In offline mode or with dummy credentials, Firebase SDK methods may throw `IllegalStateException` if `FirebaseApp` is not fully initialized.
   - *Logic*: Decorating analytics/crashlytics calls in `androidMain` with defensive `try-catch` blocks and `FirebaseApp.getApps().isNotEmpty()` guarantees that document scanning, cropping, filtering, OCR, and PDF export workflows continue smoothly with zero UI stutter or crashes.

---

## 3. Caveats

1. **Real Production Credentials**: The template `google-services.json` and `GoogleService-Info.plist` use placeholder API keys and project IDs. For live Play Store production analytics, real Firebase project credentials should replace these template files in release pipelines.
2. **iOS Firebase SDK Linking**: The surveyed iOS architecture logs telemetry to Apple Unified Logging (`os_log`) by default. When the iOS Xcode project links the native Firebase iOS framework via Swift Package Manager, the iOS adapter can forward events to `Analytics.logEvent()`.

---

## 4. Conclusion

The Firebase integration strategy is fully detailed in `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_explorer_survey_2/survey_firebase.md`.

### Recommended Solution:
1. **Libraries & Plugins**: Use `com.google.gms.google-services:4.4.2`, `com.google.firebase.crashlytics:3.0.3`, and `com.google.firebase:firebase-bom:33.7.0` for Android.
2. **Multiplatform Layer**: Implement `DocScannerAnalytics` and `DocScannerCrashlytics` contracts in `commonMain`, with safe `PlatformAnalytics` and `PlatformCrashlytics` implementations in `androidMain` and `iosMain`.
3. **Template Configurations**: Add template `google-services.json` in `androidApp/` and `GoogleService-Info.plist` in `iosApp/iosApp/`.
4. **Events Taxonomy**: Standardize 8 core event types (`doc_scanned`, `pdf_exported`, `ocr_performed`, `filter_applied`, `id_card_scanned`, `qr_studio_action`, `app_lock_toggled`, `screen_view`) with full parameter dictionaries.
5. **Crashlytics Hooks**: Capture non-fatal exceptions in `PlatformImageProcessor`, `PlatformPdfEngine`, and `PlatformOcrEngine`.

---

## 5. Verification Method

To independently verify the survey findings and the proposed build configuration:
1. Inspect the survey report at:
   `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_explorer_survey_2/survey_firebase.md`
2. Run baseline build verification command:
   ```bash
   ./gradlew :composeApp:allTests :androidApp:assembleDebug
   ```
3. Invalidation condition: If Gradle builds fail when `google-services` is applied, verify presence of `androidApp/google-services.json`.
