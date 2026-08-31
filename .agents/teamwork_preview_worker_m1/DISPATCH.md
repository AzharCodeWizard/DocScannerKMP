## 2026-08-31T18:34:51Z
You are the Firebase Integration Worker for DocScanner KMP (Milestone 1).
Your working directory is `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_worker_m1`.
Your parent is the Project Orchestrator (conversation ID: 2f6552ed-f39a-4e78-98e8-3122e06d7f0f).

Read `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/ORIGINAL_REQUEST.md`, `/Users/azhar/Documents/Projects/DocScannerKMP/PROJECT.md`, and `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_explorer_survey_2/survey_firebase.md`.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_preview_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Your Tasks:
1. In `gradle/libs.versions.toml`:
   - Add Google Services plugin (`4.4.2`), Crashlytics plugin (`3.0.3`).
   - Add Firebase BoM (`33.7.0`), `firebase-analytics`, and `firebase-crashlytics` under `[libraries]`.
2. In root `build.gradle.kts`:
   - Declare `google-services` and `firebase-crashlytics` plugins with `apply false`.
3. In `androidApp/build.gradle.kts`:
   - Apply `com.google.gms.google-services` and `com.google.firebase.crashlytics` plugins.
   - Add `implementation(platform(libs.firebase.bom))`, `implementation(libs.firebase.analytics)`, and `implementation(libs.firebase.crashlytics)`.
4. Create template configuration files:
   - `/Users/azhar/Documents/Projects/DocScannerKMP/androidApp/google-services.json` (with package `com.lufick.docscanner`).
   - `/Users/azhar/Documents/Projects/DocScannerKMP/iosApp/iosApp/GoogleService-Info.plist`.
   These ensure Gradle and iOS compilation succeed cleanly out of the box.
5. In `composeApp/src/commonMain/kotlin/com/lufick/docscanner/analytics/`:
   - Implement `DocScannerAnalytics.kt` interface (`logEvent`, `logScreenView`, `logDocScanned`, `logPdfExported`, `logOcrPerformed`, `logFilterApplied`, `logIdCardScanned`, `logQrAction`, `logAppLockToggled`).
   - Implement `DocScannerCrashlytics.kt` interface (`logException`, `setCustomKey`, `setUserId`).
6. In `composeApp/src/androidMain/kotlin/com/lufick/docscanner/analytics/`:
   - Implement `PlatformAnalytics.android.kt` and `PlatformCrashlytics.android.kt` using Google Firebase SDKs with `try-catch` / `FirebaseApp.getApps().isNotEmpty()` safety guarding so offline / unconfigured apps never crash.
7. In `composeApp/src/iosMain/kotlin/com/lufick/docscanner/analytics/`:
   - Implement safe `PlatformAnalytics.ios.kt` and `PlatformCrashlytics.ios.kt` (using Apple OSLog / Console fallback).
8. In `composeApp/src/commonMain/kotlin/com/lufick/docscanner/di/AppModule.kt`:
   - Register `DocScannerAnalytics` and `DocScannerCrashlytics` in Koin DI so they can be injected throughout the app.
9. Verify builds:
   - Run `./gradlew :composeApp:allTests` and `./gradlew :androidApp:assembleDebug`.
10. Write `handoff.md` and send a completion message back.
