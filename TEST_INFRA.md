# Test Infrastructure & Strategy — DocScanner KMP

## 1. Overview & Test Architecture

DocScanner KMP employs a robust, multi-tiered test infrastructure built on **Kotlin Multiplatform Standard Test (`kotlin.test`)** and **Coroutines Test (`kotlinx.coroutines.test`)**. The testing strategy ensures that 100% of mathematical transformation algorithms, color matrix processing, ML OCR extraction heuristics, pure Kotlin QR code generation, repository data flows, telemetry contracts, and legal constants are verified on common multiplatform targets without hardware or platform dependencies.

```
composeApp/src/commonTest/kotlin/com/lufick/docscanner/
├── engine/
│   ├── FilterEngineTest.kt        // Tier 1: 4x5 GPU ColorMatrix algorithms & pixel buffer transformations
│   ├── HomographyTest.kt          // Tier 1: 8-DOF Direct Linear Transform Gaussian solver & quad mapping
│   └── OcrParserTest.kt           // Tier 2: Structured entity extraction heuristics (Merchant, Date, Totals)
├── util/
│   └── QrCodeGeneratorTest.kt     // Tier 2: Pure Kotlin QR matrix generator, bit allocation & mask patterns
├── repository/
│   └── DocumentRepositoryTest.kt  // Tier 3: Reactive state flow, document persistence & favorites
├── analytics/
│   └── AnalyticsTest.kt           // Tier 3: Telemetry contract validation, parameter safety & offline decoupling
└── legal/
    └── LegalConstantsTest.kt      // Tier 4: Legal compliance, version codes, and OSS license integrity
```

---

## 2. Test Suite Tiers & Scope

| Tier | Component | Test File | Primary Coverage Scope |
|---|---|---|---|
| **Tier 1** | **Computer Vision & Color Engine** | `engine/FilterEngineTest.kt` | • 4x5 ColorMatrix generation for all 6 presets (`ORIGINAL`, `MAGIC_COLOR_1`, `MAGIC_COLOR_2`, `SHARP_BW`, `GRAYSCALE`, `ECO_PRINT`)<br>• Luminance Rec.709 coefficients & dynamic white lifting<br>• Pixel buffer RGB clamping (0..255) & brightness/contrast scaling |
| **Tier 1** | **Geometric Transformation Engine** | `engine/HomographyTest.kt` | • 8-DOF Direct Linear Transformation (DLT) Gaussian elimination solver<br>• Identity mapping (untransformed rectangular quads)<br>• Perspective coordinate warping and inverted homography transformations<br>• Output bounding box & aspect ratio calculation |
| **Tier 2** | **OCR Heuristics & RegEx Parser** | `engine/OcrParserTest.kt` | • Merchant name extraction heuristics with noise exclusion<br>• Multi-format date extraction (`YYYY-MM-DD`, `MM/DD/YYYY`, `DD-MMM-YYYY`)<br>• Invoice & Receipt reference number identification<br>• Subtotal, Total, and Tax amount financial parsing<br>• Malformed / noisy OCR text resilience |
| **Tier 2** | **QR Matrix Generator** | `util/QrCodeGeneratorTest.kt` | • Finder pattern geometry (7x7 modules at 3 corners)<br>• Timing pattern alternation on row 6 / col 6<br>• Alignment pattern placement for standard QR sizes (size >= 25)<br>• Multi-payload encoding: URL, Plain Text, Wi-Fi configuration, UPI payment, vCard<br>• Boolean matrix grid integrity and quiet zone safety |
| **Tier 3** | **State & Persistence** | `repository/DocumentRepositoryTest.kt` | • Reactive document queries with Kotlin Flows<br>• Document creation, retrieval, favorite toggling, and deletion<br>• In-memory cache consistency |
| **Tier 3** | **Telemetry & Diagnostics** | `analytics/AnalyticsTest.kt` | • `DocScannerAnalytics` contract implementation<br>• Event logging, screen tracking, and custom parameter map validation<br>• Safe offline fallback behavior (no network dependency) |
| **Tier 4** | **Legal & Policy Compliance** | `legal/LegalConstantsTest.kt` | • App version constant verification (`1.0.0`)<br>• Build code validation (`1`)<br>• Non-empty Privacy Policy (100% on-device guarantee verification)<br>• Non-empty Terms of Service<br>• Open Source Software licenses catalog completeness |

---

## 3. Test Execution & Verification Commands

### Execute All Multiplatform Tests
```bash
./gradlew :composeApp:allTests
```
Runs unit tests across all configured targets (Android JVM and iOS simulator architecture).

### Execute Unit Tests specifically on JVM / Android
```bash
./gradlew :composeApp:testDebugUnitTest
```

### Execute Unit Tests specifically on iOS Simulator
```bash
./gradlew :composeApp:iosSimulatorArm64Test
```

### Full Release & Debug Verification Gate
```bash
./gradlew :androidApp:assembleDebug
```
Verifies full application module compilation, manifest merging, resource packaging, and Koin dependency wiring.

---

## 4. Test Design Principles & Integrity

1. **Deterministic & Isolated**: Each test case instantiates its own isolated inputs and models without shared mutable static state.
2. **Offline-First & Self-Contained**: No network sockets, cloud emulators, or filesystem side-effects are required during test execution.
3. **Behavioral Assertion**: Tests verify mathematically verifiable output vectors (e.g. matrix dimensions, clamped RGB values, parsed financial entities) rather than implementation details.
4. **Adversarial Input Coverage**: Engines are tested against boundary conditions (zero contrast, maximum brightness, negative numbers, malformed text, empty strings, and distorted perspective coordinates).

---

## 5. CI / Quality Gate Criteria

A build is certified **TEST READY** only when:
- [x] All test classes in `commonTest` compile without warnings or deprecation errors.
- [x] 100% of test cases pass with 0 failures and 0 errors across `:composeApp:allTests`.
- [x] Android application module builds cleanly with `:androidApp:assembleDebug`.
