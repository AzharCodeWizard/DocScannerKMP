package com.lufick.docscanner.ui.legal

data class OpenSourceLicense(
    val name: String,
    val author: String,
    val licenseType: String,
    val url: String
)

object LegalConstants {
    const val APP_VERSION = "1.0.0"
    const val BUILD_CODE = 1
    const val SUPPORT_EMAIL = "support@docscanner.app"
    const val COPYRIGHT_NOTICE = "© 2026 Lufick Technologies. All rights reserved."

    val OPEN_SOURCE_LICENSES: List<OpenSourceLicense> = listOf(
        OpenSourceLicense(
            name = "Kotlin Standard Library & Coroutines",
            author = "JetBrains s.r.o.",
            licenseType = "Apache License 2.0",
            url = "https://github.com/JetBrains/kotlin"
        ),
        OpenSourceLicense(
            name = "Jetpack Compose Multiplatform",
            author = "Google LLC & JetBrains s.r.o.",
            licenseType = "Apache License 2.0",
            url = "https://github.com/JetBrains/compose-multiplatform"
        ),
        OpenSourceLicense(
            name = "Kotlinx Serialization & DateTime",
            author = "JetBrains s.r.o.",
            licenseType = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.serialization"
        ),
        OpenSourceLicense(
            name = "Koin Dependency Injection",
            author = "Kotzilla & InsertKoin.io",
            licenseType = "Apache License 2.0",
            url = "https://insert-koin.io/"
        ),
        OpenSourceLicense(
            name = "Square Okio",
            author = "Square, Inc.",
            licenseType = "Apache License 2.0",
            url = "https://github.com/square/okio"
        ),
        OpenSourceLicense(
            name = "AndroidX CameraX & Jetpack Lifecycle",
            author = "The Android Open Source Project / Google LLC",
            licenseType = "Apache License 2.0",
            url = "https://developer.android.com/jetpack"
        ),
        OpenSourceLicense(
            name = "Google Play Services ML Kit (OCR & Barcode)",
            author = "Google LLC",
            licenseType = "Android SDK & ML Kit Terms of Service",
            url = "https://developers.google.com/ml-kit"
        ),
        OpenSourceLicense(
            name = "ZXing (Zebra Crossing)",
            author = "ZXing Authors",
            licenseType = "Apache License 2.0",
            url = "https://github.com/zxing/zxing"
        ),
        OpenSourceLicense(
            name = "GitLive Firebase Multiplatform SDK",
            author = "GitLive Ltd & Firebase Authors",
            licenseType = "Apache License 2.0",
            url = "https://github.com/GitLiveApp/firebase-kotlin-sdk"
        )
    )

    val PRIVACY_POLICY_TEXT: String = """
# Privacy Policy — DocScanner KMP

**Effective Date:** September 1, 2026  
**Last Updated:** September 1, 2026

DocScanner KMP is developed and operated by Lufick Technologies ("we", "us", or "our"). We are deeply committed to protecting your privacy and ensuring you have complete control over your documents and personal data.

---

## 1. 100% On-Device Processing Guarantee

All core document scanning and processing operations are performed **100% offline directly on your local device hardware**. This includes:
- Multi-page camera capture and live laser edge detection.
- Perspective quad cropping and Gaussian Direct Linear Transform (DLT) dewarping.
- Real-time GPU ColorMatrix image enhancement (Magic Color, Sharp B&W, Eco Print).
- Machine learning optical character recognition (OCR) and structured entity extraction via on-device ML Kit models.
- QR and barcode generation and camera decoding.
- Multi-page PDF document compilation and digital e-signature embedding.

**Zero Cloud Uploads:** No document images, cropped pages, extracted text, metadata, or signatures are ever transmitted to, stored on, or analyzed by remote servers or third-party cloud services.

---

## 2. Information We Do NOT Collect

- We do not collect, read, transmit, or monetize the contents of your scanned documents.
- We do not require account creation, logins, phone numbers, or social media links to use DocScanner KMP.
- We do not track your location or access personal contacts.

---

## 3. Local Vault Storage & Biometric Security

- **Isolated App Sandboxing:** All scanned documents, thumbnails, and cache files are stored exclusively within the application's private, encrypted sandboxed storage directory on your device.
- **Biometric & PIN Lock:** When App Lock is enabled, access to your local document vault is guarded by your device's hardware-backed biometric authentication (fingerprint / Face Unlock) or a custom 4-digit security PIN. Biometric authentication is handled entirely by your operating system's secure keystore; biometric credentials never leave the secure enclave.

---

## 4. Device Permissions & Why We Need Them

DocScanner KMP requests only the minimum device permissions necessary to provide scanning and document export functions:
- **Camera (`CAMERA`):** Required exclusively to capture document pages, detect document boundaries in real time, and scan QR/barcodes via the viewfinder.
- **Photos & Media Storage (`READ_MEDIA_IMAGES` / `WRITE_EXTERNAL_STORAGE`):** Used strictly when you explicitly choose to import photos from your gallery or export generated PDF / JPEG documents to external device folders.
- **Biometric Hardware (`USE_BIOMETRIC`):** Used solely to unlock your local document vault when Biometric Lock is turned on in Settings.

---

## 5. Diagnostic Telemetry & Crash Reports

To help us maintain application stability, performance, and resolve unexpected bugs, DocScanner KMP may collect anonymized diagnostic telemetry via Firebase Analytics and Crashlytics:
- **Aggregated Event Metrics:** High-level non-personal action counts (e.g. document scanned count, PDF export triggered, filter preset selected).
- **Crash Reports:** Non-fatal stack traces, operating system versions, and device hardware models.
- **No Document Data in Diagnostics:** Crash reports and analytics payloads **NEVER** contain document images, file names, OCR text, or user signatures.
- Telemetry operates gracefully in offline mode, buffering or discarding reports if internet access is unavailable.

---

## 6. Data Retention & User Control

You maintain total control over your data at all times:
- You can delete any document, page, or tag at any time from within the app, permanently removing the corresponding files from your local storage.
- You can clear temporary image caches at any time via **Settings > Storage & Camera Hardware > Clear Temporary Scans Cache**.
- Uninstalling the application completely deletes all local sandboxed documents and application data from your device.

---

## 7. Changes to This Privacy Policy

We may update our Privacy Policy periodically. Any updates will be reflected within this in-app screen and published with updated release notes.

---

## 8. Contact Us

If you have questions, concerns, or feedback regarding this Privacy Policy or our on-device privacy architecture, please contact our privacy and engineering team:
- **Email:** support@docscanner.app
- **Developer:** Lufick Technologies
""".trimIndent()

    val TERMS_OF_SERVICE_TEXT: String = """
# Terms of Service — DocScanner KMP

**Effective Date:** September 1, 2026  
**Last Updated:** September 1, 2026

Please read these Terms of Service ("Terms") carefully before using DocScanner KMP ("the Application"), developed by Lufick Technologies ("we", "us", or "our").

By downloading, installing, accessing, or using DocScanner KMP, you agree to be bound by these Terms. If you do not agree, do not install or use the Application.

---

## 1. Grant of License

Lufick Technologies grants you a personal, worldwide, royalty-free, non-exclusive, non-transferable, and revocable license to use DocScanner KMP on your compatible devices strictly for personal and commercial document digitizing, PDF editing, OCR extraction, and signature creation in accordance with these Terms.

---

## 2. User Intellectual Property & Ownership

- **100% User Ownership:** You retain full, exclusive intellectual property rights and title to all documents, photographs, signatures, scanned images, notes, and PDF exports created or processed using the Application.
- **No License to Us:** Lufick Technologies claims no intellectual property rights or ownership interest in any user content digitized through DocScanner KMP.

---

## 3. Cryptographic Security & Password Ownership

- **AES PDF Encryption:** When applying a password to generated PDF documents, the Application utilizes standard AES encryption executed locally on your device hardware.
- **Client-Managed Keys:** Encryption keys and passwords are never transmitted to or stored by Lufick Technologies. **We cannot recover, reset, or bypass forgotten document passwords.** You are solely responsible for remembering your passwords and creating secure off-device backups of critical documents.

---

## 4. Permitted and Prohibited Conduct

You agree that you will not use the Application to:
- Digitize, forge, or alter government identification cards, currency, or copyrighted materials in violation of applicable laws.
- Reverse engineer, decompile, disassemble, or attempt to extract source code from proprietary portions of the Application, except to the extent permitted by applicable open-source licenses.
- Use the Application for any unlawful, deceptive, or fraudulent purpose.

---

## 5. Third-Party Open Source Software

DocScanner KMP incorporates open-source software libraries and frameworks. Each open-source component is governed by its respective license terms (e.g. Apache License 2.0). Nothing in these Terms limits your rights under the terms of any applicable open-source license. Full license texts and attributions are viewable directly in **Settings > Open Source Licenses**.

---

## 6. Disclaimer of Warranties

THE APPLICATION IS PROVIDED ON AN "AS IS" AND "AS AVAILABLE" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT, OR ACCURACY OF OCR TEXT RECOGNITION. 

WHILE WE STRIVE FOR THE HIGHEST ACCURACY WITH HARDWARE PERSPECTIVE DEWARPING AND ON-DEVICE ML OCR, YOU ACKNOWLEDGE THAT OCR EXTRACTION MAY OCCASIONALLY CONTAIN ERRORS AND SHOULD BE VERIFIED BEFORE USE IN CRITICAL FINANCIAL OR LEGAL WORKFLOWS.

---

## 7. Limitation of Liability

TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, IN NO EVENT SHALL LUFICK TECHNOLOGIES, ITS AFFILIATES, DIRECTORS, EMPLOYEES, OR AGENTS BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES, INCLUDING WITHOUT LIMITATION LOSS OF PROFITS, DATA, USE, GOODWILL, OR OTHER INTANGIBLE LOSSES RESULTING FROM YOUR USE OF OR INABILITY TO USE THE APPLICATION.

---

## 8. Governing Law & Dispute Resolution

These Terms shall be governed by and construed in accordance with the laws of the jurisdiction in which Lufick Technologies operates, without regard to its conflict of law principles.

---

## 9. Modifications to Terms

We reserve the right to revise or modify these Terms at any time. Continued use of DocScanner KMP following any updates constitutes your acceptance of the revised Terms.

---

## 10. Contact Information

For inquiries, support, or legal questions concerning these Terms, please reach out to:
- **Email:** support@docscanner.app
- **Developer:** Lufick Technologies
""".trimIndent()
}
