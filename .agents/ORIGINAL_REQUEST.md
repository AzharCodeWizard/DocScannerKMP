# Original User Request

## 2026-08-31T18:28:21Z

Bring DocScanner KMP to 100% production readiness: integrate Firebase Analytics and Crashlytics, add complete in-app About and Legal/Policy pages, verify all scanning and PDF tool workflows end-to-end, and generate a comprehensive Google Play Store release asset bundle (screenshots, graphics, icons, and store listing metadata).

Working directory: `/Users/azhar/Documents/Projects/DocScannerKMP`
Integrity mode: development

## Requirements

### R1. Complete End-to-End Functionality & Polish
Ensure all scanning and studio workflows operate flawlessly and crash-free with real data:
- Multi-page camera scanning, smooth auto edge detection, perspective dewarp, and GPU ColorMatrix filters.
- 2-in-1 ID Card scanning (front/back capture stitched onto a single page).
- QR & Barcode Studio: Google Pay style auto-zooming scanner and multi-type QR code generator/vault.
- On-device ML Kit OCR text extraction, PDF generation & tools (AES password lock, custom watermarking, e-signature drawing pad), and document search/tagging/folders.

### R2. Firebase Analytics & Crashlytics Integration
- Integrate official Firebase / GitLive KMP Firebase dependencies for Android & iOS.
- Configure Firebase Analytics for core user events (document scanned, PDF exported, OCR performed, filter applied).
- Configure Firebase Crashlytics for non-fatal exception logging and crash reporting.
- Ensure the app builds and runs cleanly in offline mode or when `google-services.json` / `GoogleService-Info.plist` is not yet configured.

### R3. About, Legal & Policy Pages
- Create a dedicated **About & Settings** screen accessible from the top bar.
- Add sections for App Version, Build Number, Open Source Licenses, Privacy Policy, and Terms of Service (viewable directly offline in-app).
- Add support contact and feedback trigger.

### R4. Google Play Store Release Asset Bundle
Create a complete publication bundle in `playstore_assets/`:
- **App Icon**: `icon_512x512.png` (high-res 512x512 app launcher icon).
- **Feature Graphic**: `feature_graphic_1024x500.png` (1024x500 promo banner).
- **Play Store Screenshots**: High-res mockups (`1080x2400` PNGs) showcasing:
  1. Home Dashboard & Document Manager
  2. Camera Scanner & Edge Detection
  3. Adjust & Crop Dewarping
  4. GPU Color Preset Filters & Sliders
  5. PDF Tools & E-Signature Pad
  6. 2-in-1 ID Card Stitched Scan
  7. QR & Barcode Studio
  8. ML Kit OCR Text Extractor
- **Store Copy (`store_listing.md`)**:
  - App Title (under 30 chars).
  - Short Description (under 80 chars).
  - Full Description (formatted markdown with features, highlights, privacy guarantees, keywords).
  - Release Notes for version 1.0.0.

## Acceptance Criteria

### Verification & Automated Testing
- [ ] `./gradlew :composeApp:allTests` passes with 0 failures.
- [ ] `./gradlew :androidApp:assembleDebug` builds successfully.
- [ ] Firebase Analytics and Crashlytics initialized safely without breaking builds or runtime when offline.
- [ ] About screen, Privacy Policy, and Terms of Service accessible and rendered cleanly in navigation.
- [ ] `playstore_assets/` contains all generated images (`icon_512x512.png`, `feature_graphic_1024x500.png`, screenshot images) and `store_listing.md`.
