package com.lufick.docscanner.engine

import com.lufick.docscanner.model.ExtractedEntity

/**
 * Intelligent Document & Receipt OCR Entity Extraction Engine.
 */
object OcrParser {

    private val DATE_REGEX = Regex(
        """\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]* \d{1,2},? \d{4})\b""",
        RegexOption.IGNORE_CASE
    )

    private val TOTAL_REGEX = Regex(
        """(?:TOTAL|AMOUNT DUE|BALANCE DUE|GRAND TOTAL|NET AMOUNT)[\s:]*[$€£₹]?\s*(\d+[.,]\d{2})""",
        RegexOption.IGNORE_CASE
    )

    private val INVOICE_REGEX = Regex(
        """(?:INVOICE|INV|RECEIPT|BILL|ORDER)\s*(?:NO|NUM|NUMBER|#)?[\s:]*([A-Z0-9_-]{4,20})""",
        RegexOption.IGNORE_CASE
    )

    private val TAX_REGEX = Regex(
        """(?:TAX|VAT|GST|HST)[\s:]*[$€£₹]?\s*(\d+[.,]\d{2})""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Parse raw OCR text lines and extract structured metadata entities.
     */
    fun extractEntities(rawText: String): List<ExtractedEntity> {
        val entities = mutableListOf<ExtractedEntity>()
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // 1. Merchant / Organization (heuristic: top prominent line)
        val merchantCandidate = lines.firstOrNull { line ->
            line.length in 3..40 &&
                    !line.contains(Regex("""\d{3,}""")) &&
                    !line.startsWith("Date", ignoreCase = true) &&
                    !line.startsWith("Invoice", ignoreCase = true)
        }
        if (merchantCandidate != null) {
            entities.add(ExtractedEntity(key = "Merchant", value = merchantCandidate, category = "General"))
        }

        // 2. Date
        val dateMatch = DATE_REGEX.find(rawText)
        if (dateMatch != null) {
            entities.add(ExtractedEntity(key = "Date", value = dateMatch.value, category = "Date"))
        }

        // 3. Invoice / Reference Number
        val invoiceMatch = INVOICE_REGEX.find(rawText)
        if (invoiceMatch != null) {
            val invValue = invoiceMatch.groups[1]?.value ?: invoiceMatch.value
            entities.add(ExtractedEntity(key = "Invoice #", value = invValue, category = "Reference"))
        }

        // 4. Total Amount
        val totalMatch = TOTAL_REGEX.find(rawText)
        if (totalMatch != null) {
            val totalValue = totalMatch.groups[1]?.value ?: totalMatch.value
            entities.add(ExtractedEntity(key = "Total", value = "$$totalValue", category = "Financial"))
        }

        // 5. Tax / GST / VAT
        val taxMatch = TAX_REGEX.find(rawText)
        if (taxMatch != null) {
            val taxValue = taxMatch.groups[1]?.value ?: taxMatch.value
            entities.add(ExtractedEntity(key = "Tax", value = "$$taxValue", category = "Financial"))
        }

        return entities
    }
}
