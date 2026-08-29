package com.lufick.docscanner.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OcrParserTest {

    @Test
    fun testEntityExtraction() {
        val sampleText = """WHOLE FOODS MARKET
Date: Oct 25, 2026
Invoice No: 84920
Item 1: $10.00
Tax: $0.80
Total: $10.80"""

        val entities = OcrParser.extractEntities(sampleText)
        assertNotNull(entities)
        
        val totalEntity = entities.find { it.key == "Total" }
        assertNotNull(totalEntity)
        assertEquals("$10.80", totalEntity.value)

        val dateEntity = entities.find { it.key == "Date" }
        assertNotNull(dateEntity)
        assertEquals("Oct 25, 2026", dateEntity.value)
    }
}
