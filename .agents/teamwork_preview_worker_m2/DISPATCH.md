## 2026-09-01T00:04:51Z

You are the About & Legal UI Worker for DocScanner KMP (Milestone 2).
Your working directory is `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_worker_m2`.
Your parent is the Project Orchestrator (conversation ID: 2f6552ed-f39a-4e78-98e8-3122e06d7f0f).

Read `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/ORIGINAL_REQUEST.md`, `/Users/azhar/Documents/Projects/DocScannerKMP/PROJECT.md`, and `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_spec_miner_survey_3/survey_spec_assets.md`.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_preview_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Your Tasks:
1. In `composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/legal/`:
   - Create `LegalConstants.kt` containing:
     - `APP_VERSION = "1.0.0"`, `BUILD_CODE = 1`.
     - `OPEN_SOURCE_LICENSES`: full list of open source dependencies (Kotlin, Jetpack Compose Multiplatform, Koin, Coroutines, ML Kit, CameraX, ZXing, Firebase).
     - `PRIVACY_POLICY_TEXT`: full 100% on-device offline Privacy Policy explaining 0 cloud uploads, on-device ML OCR, biometric lock, and local storage.
     - `TERMS_OF_SERVICE_TEXT`: full offline Terms of Service text.
   - Create `LegalDialogs.kt`:
     - Composables for interactive modal/dialog views for Open Source Licenses, Privacy Policy, and Terms of Service (clean scrollable dialogs with dismiss button).
2. In `composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/screens/SettingsScreen.kt`:
   - Update the About & Legal card to show dynamic App Version (1.0.0) and Build Code (1).
   - Add list items / buttons for:
     - "Open Source Licenses" -> opens Licenses dialog.
     - "Privacy Policy" -> opens Privacy Policy dialog.
     - "Terms of Service" -> opens Terms of Service dialog.
     - "Contact Support & Feedback" -> triggers support email intent to `support@docscanner.app` with diagnostic info (App Version, Platform).
3. Verify builds:
   - Run `./gradlew :composeApp:allTests` and `./gradlew :androidApp:assembleDebug`.
4. Write `handoff.md` and send a completion message back.
