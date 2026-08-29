package com.lufick.docscanner.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.model.FilterType
import com.lufick.docscanner.theme.LufickEmerald

enum class DocumentTemplateType {
    RECEIPT,
    LEASE_CONTRACT,
    ID_CARD,
    PASSPORT,
    MEDICAL_PRESCRIPTION
}

@Composable
fun RenderedDocumentView(
    modifier: Modifier = Modifier,
    templateType: DocumentTemplateType = DocumentTemplateType.RECEIPT,
    filterType: FilterType = FilterType.ORIGINAL,
    brightness: Float = 1.0f,
    contrast: Float = 1.0f,
    rotationDegrees: Int = 0
) {
    val bgColor = when (filterType) {
        FilterType.ORIGINAL -> Color(0xFFFEF3C7) // Warm paper photo
        FilterType.MAGIC_COLOR_1 -> Color(0xFFFFFFFF) // Pure white
        FilterType.MAGIC_COLOR_2 -> Color(0xFFFAFAFA)
        FilterType.SHARP_BW -> Color(0xFFFFFFFF)
        FilterType.GRAYSCALE -> Color(0xFFF1F5F9)
        FilterType.ECO_PRINT -> Color(0xFFFFFFFF)
    }

    val textColor = when (filterType) {
        FilterType.SHARP_BW -> Color(0xFF000000)
        FilterType.GRAYSCALE -> Color(0xFF334155)
        else -> Color(0xFF1E293B)
    }

    val accentColor = when (filterType) {
        FilterType.SHARP_BW, FilterType.GRAYSCALE -> Color(0xFF000000)
        FilterType.MAGIC_COLOR_1 -> Color(0xFF059669) // Vibrant emerald
        FilterType.MAGIC_COLOR_2 -> Color(0xFF2563EB) // Vibrant blue
        else -> Color(0xFF0D9488)
    }

    Box(
        modifier = modifier
            .rotate(rotationDegrees.toFloat())
            .shadow(12.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(16.dp)
    ) {
        when (templateType) {
            DocumentTemplateType.RECEIPT -> {
                ReceiptTemplate(textColor = textColor, accentColor = accentColor, filterType = filterType)
            }
            DocumentTemplateType.LEASE_CONTRACT -> {
                LeaseTemplate(textColor = textColor, accentColor = accentColor)
            }
            DocumentTemplateType.ID_CARD -> {
                IdCardTemplate(textColor = textColor, accentColor = accentColor)
            }
            DocumentTemplateType.PASSPORT -> {
                PassportTemplate(textColor = textColor, accentColor = accentColor)
            }
            DocumentTemplateType.MEDICAL_PRESCRIPTION -> {
                PrescriptionTemplate(textColor = textColor, accentColor = accentColor)
            }
        }
    }
}

@Composable
private fun ReceiptTemplate(textColor: Color, accentColor: Color, filterType: FilterType) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "WHOLE FOODS MARKET",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textColor,
                letterSpacing = 1.sp
            )
            Text(
                text = "ORGANIC & NATURAL FOODS",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = "Store #10492 • Austin, TX",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = textColor.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("DATE: 10/25/2026", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = textColor)
                Text("TIME: 14:32", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = textColor)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CASHIER: SARAH M.", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = textColor)
                Text("INV: #849204", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = textColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dashed Divider
            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                drawLine(
                    color = textColor.copy(alpha = 0.5f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Line Items
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ReceiptLineItem("1x ORGANIC OAT MILK 64OZ", "$4.99", textColor)
                ReceiptLineItem("2x HASS AVOCADOS FRESH", "$3.50", textColor)
                ReceiptLineItem("1x SOURDOUGH ARTISAN BREAD", "$5.25", textColor)
                ReceiptLineItem("1x CEREMONIAL MATCHA TIN", "$12.99", textColor)
                ReceiptLineItem("1x ALMOND BUTTER JAR", "$7.49", textColor)
            }
        }

        Column {
            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                drawLine(
                    color = textColor.copy(alpha = 0.5f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SUBTOTAL", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor)
                Text("$34.22", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TAX (8.25%)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor)
                Text("$2.82", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TOTAL DUE", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text("$37.04", fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barcode Representation
            Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
                val barCount = 42
                val barW = size.width / barCount
                for (i in 0 until barCount) {
                    if (i % 2 == 0 || i % 5 == 0) {
                        drawRect(
                            color = textColor,
                            topLeft = Offset(i * barW, 0f),
                            size = androidx.compose.ui.geometry.Size(barW * 0.7f, size.height)
                        )
                    }
                }
            }

            Text(
                text = "* 849204928104 *",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = textColor.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ReceiptLineItem(name: String, price: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = color, maxLines = 1)
        Text(price, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun LeaseTemplate(textColor: Color, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("RESIDENTIAL LEASE AGREEMENT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
            Text("STATE OF CALIFORNIA • STANDARD CONTRACT", fontSize = 8.sp, color = textColor.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "This agreement is made on October 18, 2026, by and between APEX PROPERTY MANAGEMENT (\"Landlord\") and JOHN DOE (\"Tenant\").",
                fontSize = 8.sp,
                lineHeight = 11.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "1. PREMISES: Landlord leases to Tenant Apt 4B, 742 Evergreen Terrace, San Francisco, CA 94107.\n" +
                "2. TERM: 12 months beginning Nov 1, 2026.\n" +
                "3. RENT: Monthly rent of $2,450.00 due on 1st of month.\n" +
                "4. SECURITY DEPOSIT: $2,450.00 held in escrow.",
                fontSize = 8.sp,
                lineHeight = 11.sp,
                color = textColor
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Box(modifier = Modifier.width(90.dp).height(1.dp).background(textColor))
                Text("Landlord Signature", fontSize = 7.sp, color = textColor)
            }
            Column {
                Box(modifier = Modifier.width(90.dp).height(1.dp).background(textColor))
                Text("Tenant Signature", fontSize = 7.sp, color = textColor)
            }
        }
    }
}

@Composable
private fun IdCardTemplate(textColor: Color, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("DRIVER LICENSE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = accentColor)
            Text("USA • CALIFORNIA", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = textColor)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(width = 64.dp, height = 80.dp).background(Color(0xFFCBD5E1), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("PHOTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("LN: D9482019", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text("EXP: 10/25/2030", fontSize = 8.sp, color = textColor)
                Text("FN: JANE ALEXIS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text("DOB: 07/14/1994", fontSize = 8.sp, color = textColor)
                Text("CLASS: C  REST: NONE", fontSize = 8.sp, color = textColor)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(textColor.copy(alpha = 0.8f)))
    }
}

@Composable
private fun PassportTemplate(textColor: Color, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("PASSPORT / PASSEPORT", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textColor)
            Text("USA", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = accentColor)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(60.dp, 75.dp).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
                Text("PHOTO", fontSize = 8.sp, color = Color.Gray)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Type: P  Code: USA  Pass No: A9482019", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text("Surname: DOE", fontSize = 8.sp, color = textColor)
                Text("Given Names: JANE ALEXIS", fontSize = 8.sp, color = textColor)
                Text("Nationality: UNITED STATES OF AMERICA", fontSize = 7.sp, color = textColor)
            }
        }
        Text(
            "P<USADOE<<JANE<ALEXIS<<<<<<<<<<<<<<<<<<<<<<<\nA948201940USA9407142F3401015<<<<<<<<<<<<<<04",
            fontFamily = FontFamily.Monospace,
            fontSize = 7.sp,
            color = textColor,
            lineHeight = 9.sp
        )
    }
}

@Composable
private fun PrescriptionTemplate(textColor: Color, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("DR. ROBERT CHEN, MD", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = textColor)
            Text("Rx #49281", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = accentColor)
        }
        Text("PATIENT: JOHN DOE  |  AGE: 34  |  DATE: 10/25/2026", fontSize = 8.sp, color = textColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "1. Amoxicillin 500mg - 1 capsule PO TID x 10 days\n" +
            "2. Ibuprofen 400mg - 1 tablet PRN for pain\n" +
            "Refills: 0  |  DAW: 1",
            fontSize = 9.sp,
            lineHeight = 13.sp,
            color = textColor
        )
        Text("Doctor's Signature: Dr. R. Chen", fontSize = 8.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = textColor)
    }
}
