package com.lufick.docscanner.ui.legal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LegalTest {

    @Test
    fun testLegalConstantsVersionAndBuild() {
        assertEquals("1.0.0", LegalConstants.APP_VERSION)
        assertEquals(1, LegalConstants.BUILD_CODE)
        assertEquals("support@docscanner.app", LegalConstants.SUPPORT_EMAIL)
        assertTrue(LegalConstants.COPYRIGHT_NOTICE.contains("2026 Lufick Technologies"))
    }

    @Test
    fun testOpenSourceLicensesListCompleteness() {
        val licenses = LegalConstants.OPEN_SOURCE_LICENSES
        assertTrue(licenses.isNotEmpty())
        assertTrue(licenses.size >= 8)

        val names = licenses.map { it.name }
        assertTrue(names.any { it.contains("Kotlin", ignoreCase = true) })
        assertTrue(names.any { it.contains("Compose", ignoreCase = true) })
        assertTrue(names.any { it.contains("Koin", ignoreCase = true) })
        assertTrue(names.any { it.contains("Okio", ignoreCase = true) })
        assertTrue(names.any { it.contains("CameraX", ignoreCase = true) })
        assertTrue(names.any { it.contains("ML Kit", ignoreCase = true) })
        assertTrue(names.any { it.contains("ZXing", ignoreCase = true) })
        assertTrue(names.any { it.contains("Firebase", ignoreCase = true) })

        licenses.forEach { license ->
            assertTrue(license.name.isNotBlank(), "License name must not be blank")
            assertTrue(license.author.isNotBlank(), "License author must not be blank")
            assertTrue(license.licenseType.isNotBlank(), "License type must not be blank")
            assertTrue(license.url.startsWith("http://") || license.url.startsWith("https://"), "License URL must be valid")
        }
    }

    @Test
    fun testPrivacyPolicyContentGuarantees() {
        val policy = LegalConstants.PRIVACY_POLICY_TEXT
        assertTrue(policy.isNotBlank())
        assertTrue(policy.contains("100% On-Device Processing Guarantee", ignoreCase = true))
        assertTrue(policy.contains("Zero Cloud Uploads", ignoreCase = true))
        assertTrue(policy.contains("Biometric", ignoreCase = true))
        assertTrue(policy.contains("Camera", ignoreCase = true))
        assertTrue(policy.contains("Firebase Analytics", ignoreCase = true))
        assertTrue(policy.contains("support@docscanner.app", ignoreCase = true))
    }

    @Test
    fun testTermsOfServiceContentGuarantees() {
        val terms = LegalConstants.TERMS_OF_SERVICE_TEXT
        assertTrue(terms.isNotBlank())
        assertTrue(terms.contains("Grant of License", ignoreCase = true))
        assertTrue(terms.contains("User Intellectual Property & Ownership", ignoreCase = true))
        assertTrue(terms.contains("AES PDF Encryption", ignoreCase = true))
        assertTrue(terms.contains("Disclaimer of Warranties", ignoreCase = true))
        assertTrue(terms.contains("Limitation of Liability", ignoreCase = true))
        assertTrue(terms.contains("support@docscanner.app", ignoreCase = true))
    }
}
