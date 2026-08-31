## 2026-08-31T18:34:51Z
You are the E2E Test Writer for DocScanner KMP (Milestone 4 / E2E Track).
Your working directory is `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/teamwork_preview_test_writer_m4`.
Your parent is the Project Orchestrator (conversation ID: 2f6552ed-f39a-4e78-98e8-3122e06d7f0f).

Read `/Users/azhar/Documents/Projects/DocScannerKMP/.agents/ORIGINAL_REQUEST.md` and `/Users/azhar/Documents/Projects/DocScannerKMP/PROJECT.md`.

Your Tasks:
1. Create `/Users/azhar/Documents/Projects/DocScannerKMP/TEST_INFRA.md` following the template in PROJECT.md.
2. In `composeApp/src/commonTest/kotlin/com/lufick/docscanner/`:
   - Create `engine/FilterEngineTest.kt`:
     - Test ColorMatrix generation for all presets (`ORIGINAL`, `MAGIC_COLOR_1`, `MAGIC_COLOR_2`, `SHARP_BW`, `GRAYSCALE`, `ECO_PRINT`).
     - Test brightness and contrast scaling factors, matrix array length (20 elements), clamp limits.
   - Create `engine/HomographyTest.kt`:
     - Test 8-DOF Direct Linear Transform Gaussian solver.
     - Test identity mapping (corners unchanged).
     - Test perspective warp coordinate transformation, bounding box calculations.
   - Create `engine/OcrParserTest.kt`:
     - Test receipt/invoice text extraction: Merchant name heuristic, date regex formats (YYYY-MM-DD, MM/DD/YYYY, DD-MMM-YYYY), Invoice number patterns, Subtotal, Total, and Tax amount parsing.
     - Test robustness against malformed/noisy text.
   - Create `util/QrCodeGeneratorTest.kt`:
     - Test QR matrix generation across payload types: plain text, URL, Wi-Fi config (`WIFI:S:...`), UPI payment link (`upi://pay?...`), vCard.
     - Test matrix dimensions, quiet zone padding, boolean grid integrity.
   - Create `analytics/AnalyticsTest.kt`:
     - Test `DocScannerAnalytics` contract implementation, event logging verification, parameter validation.
   - Create `legal/LegalConstantsTest.kt`:
     - Test version constant `1.0.0`, build code `1`, non-empty Privacy Policy, Terms of Service, and OSS licenses list.
3. Run `./gradlew :composeApp:allTests` and `./gradlew :androidApp:assembleDebug` and verify all tests pass with 0 failures.
4. Create `/Users/azhar/Documents/Projects/DocScannerKMP/TEST_READY.md` summarizing the test suite coverage across Tiers 1-4.
5. Write `handoff.md` and send a completion message back.
