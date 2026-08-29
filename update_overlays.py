with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/components/CameraOverlays.kt', 'r') as f:
    content = f.read()

# Update BookGuideOverlay
old_book = """        drawLine(
            color = LufickEmerald,
            start = Offset(centerX, h * 0.15f),
            end = Offset(centerX, h * 0.82f),
            strokeWidth = 3.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
        )

        drawRoundRect(
            color = LufickEmerald.copy(alpha = 0.4f),
            topLeft = Offset(24.dp.toPx(), h * 0.18f),
            size = Size(centerX - 36.dp.toPx(), h * 0.60f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawRoundRect(
            color = LufickEmerald.copy(alpha = 0.4f),
            topLeft = Offset(centerX + 12.dp.toPx(), h * 0.18f),
            size = Size(centerX - 36.dp.toPx(), h * 0.60f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )"""

new_book = """        val topY = h * 0.13f
        val botY = h * 0.62f
        val bookH = botY - topY

        drawLine(
            color = LufickEmerald,
            start = Offset(centerX, topY),
            end = Offset(centerX, botY),
            strokeWidth = 3.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
        )

        drawRoundRect(
            color = LufickEmerald.copy(alpha = 0.4f),
            topLeft = Offset(24.dp.toPx(), topY + 12.dp.toPx()),
            size = Size(centerX - 36.dp.toPx(), bookH - 24.dp.toPx()),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawRoundRect(
            color = LufickEmerald.copy(alpha = 0.4f),
            topLeft = Offset(centerX + 12.dp.toPx(), topY + 12.dp.toPx()),
            size = Size(centerX - 36.dp.toPx(), bookH - 24.dp.toPx()),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )"""

content = content.replace(old_book, new_book)

# Update IdCardGuideOverlay
old_id = """        val cardWidth = (w * 0.88f).coerceAtMost(360.dp.toPx())
        val cardHeight = cardWidth / 1.586f
        val cardLeft = (w - cardWidth) / 2f
        val cardTop = (h - cardHeight) / 2f - 40.dp.toPx()"""

new_id = """        val cardWidth = (w * 0.88f).coerceAtMost(340.dp.toPx())
        val cardHeight = cardWidth / 1.586f
        val cardLeft = (w - cardWidth) / 2f
        val cardTop = h * 0.18f"""

content = content.replace(old_id, new_id)

# Update PassportGuideOverlay
old_pass = """        val passportWidth = (w * 0.88f).coerceAtMost(360.dp.toPx())
        val passportHeight = passportWidth * 1.42f
        val pLeft = (w - passportWidth) / 2f
        val pTop = (h - passportHeight) / 2f - 30.dp.toPx()"""

new_pass = """        val passportWidth = (w * 0.82f).coerceAtMost(320.dp.toPx())
        val passportHeight = (passportWidth * 1.35f).coerceAtMost(h * 0.48f)
        val pLeft = (w - passportWidth) / 2f
        val pTop = h * 0.14f"""

content = content.replace(old_pass, new_pass)

# Update QrScannerOverlay
old_qr = """        val boxSize = 250.dp.toPx()
        val left = (w - boxSize) / 2f
        val top = (h - boxSize) / 2f - 40.dp.toPx()"""

new_qr = """        val boxSize = 240.dp.toPx()
        val left = (w - boxSize) / 2f
        val top = h * 0.20f"""

content = content.replace(old_qr, new_qr)

with open('composeApp/src/commonMain/kotlin/com/lufick/docscanner/ui/components/CameraOverlays.kt', 'w') as f:
    f.write(content)
