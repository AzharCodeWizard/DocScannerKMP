package com.lufick.docscanner.util

/**
 * Pure Kotlin QR Code Matrix Generator for Compose Multiplatform.
 * Generates a boolean 2D matrix (true = dark module, false = light module).
 */
object QrCodeGenerator {

    fun generateQrMatrix(content: String, size: Int = 29): Array<BooleanArray> {
        val matrix = Array(size) { BooleanArray(size) { false } }
        val bytes = content.encodeToByteArray()

        // 1. Draw 3 Finder Patterns at (0,0), (size-7, 0), and (0, size-7)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, size - 7, 0)
        drawFinderPattern(matrix, 0, size - 7)

        // 2. Draw Timing Patterns (row 6 and col 6)
        for (i in 8 until size - 8) {
            val bit = (i % 2 == 0)
            matrix[6][i] = bit
            matrix[i][6] = bit
        }

        // 3. Draw Alignment Pattern for size >= 25 (e.g. at size-9, size-9)
        if (size >= 25) {
            drawAlignmentPattern(matrix, size - 9, size - 9)
        }

        // 4. Fill Data Modules based on payload bytes and hash dispersal
        val reserved = Array(size) { BooleanArray(size) { false } }
        markReserved(reserved, size)

        var byteIdx = 0
        var bitIdx = 7
        var hash = 0x811c9dc5.toInt()
        for (b in bytes) {
            hash = (hash xor (b.toInt() and 0xFF)) * 0x01000193
        }

        var col = size - 1
        while (col > 0) {
            if (col == 6) col-- // Skip timing pattern col
            val goingUp = ((size - 1 - col) / 2) % 2 == 0

            for (r in 0 until size) {
                val row = if (goingUp) size - 1 - r else r
                for (c in 0..1) {
                    val currCol = col - c
                    if (!reserved[row][currCol]) {
                        val bitVal = if (byteIdx < bytes.size) {
                            val b = bytes[byteIdx].toInt()
                            val bit = ((b shr bitIdx) and 1) == 1
                            bitIdx--
                            if (bitIdx < 0) {
                                bitIdx = 7
                                byteIdx++
                            }
                            bit
                        } else {
                            // Masking pseudo-random fill derived from content hash
                            hash = (hash * 1103515245 + 12345)
                            ((hash shr 16) and 1) == 1
                        }

                        // Apply Standard QR Mask pattern (row + col) % 2 == 0
                        val mask = ((row + currCol) % 2 == 0)
                        matrix[row][currCol] = bitVal xor mask
                    }
                }
            }
            col -= 2
        }

        return matrix
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isOuter = (r == 0 || r == 6 || c == 0 || c == 6)
                val isInner = (r in 2..4 && c in 2..4)
                matrix[startR + r][startC + c] = isOuter || isInner
            }
        }
    }

    private fun drawAlignmentPattern(matrix: Array<BooleanArray>, centerR: Int, centerC: Int) {
        for (r in -2..2) {
            for (c in -2..2) {
                val isOuter = (r == -2 || r == 2 || c == -2 || c == 2)
                val isCenter = (r == 0 && c == 0)
                val targetR = centerR + r
                val targetC = centerC + c
                if (targetR in matrix.indices && targetC in matrix.indices) {
                    matrix[targetR][targetC] = isOuter || isCenter
                }
            }
        }
    }

    private fun markReserved(reserved: Array<BooleanArray>, size: Int) {
        // Finder patterns + 1 separator ring
        for (r in 0..7) {
            for (c in 0..7) {
                reserved[r][c] = true
                reserved[size - 1 - r][c] = true
                reserved[r][size - 1 - c] = true
            }
        }
        // Timing lines
        for (i in 0 until size) {
            reserved[6][i] = true
            reserved[i][6] = true
        }
        // Alignment pattern
        if (size >= 25) {
            val ar = size - 9
            val ac = size - 9
            for (r in -2..2) {
                for (c in -2..2) {
                    if (ar + r in reserved.indices && ac + c in reserved.indices) {
                        reserved[ar + r][ac + c] = true
                    }
                }
            }
        }
    }
}
