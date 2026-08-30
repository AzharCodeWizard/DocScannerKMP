package com.lufick.docscanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lufick.docscanner.theme.LufickEmerald

@Composable
fun AppLockOverlay(
    isLocked: Boolean,
    isBiometricEnabled: Boolean = true,
    onUnlockWithPin: (pin: String) -> Boolean,
    onUnlockWithBiometric: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isLocked,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090D16))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Lock Icon
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(if (isError) Color(0xFFEF4444).copy(alpha = 0.2f) else LufickEmerald.copy(alpha = 0.18f))
                        .border(2.dp, if (isError) Color(0xFFEF4444) else LufickEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Vault Locked",
                        tint = if (isError) Color(0xFFEF4444) else LufickEmerald,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "DocScanner Vault",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isError) "Incorrect PIN. Try again (or 0000)" else "Enter 4-Digit PIN or Fingerprint",
                    fontSize = 13.sp,
                    color = if (isError) Color(0xFFEF4444) else Color.LightGray
                )

                Spacer(modifier = Modifier.height(30.dp))

                // PIN Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..3) {
                        val isFilled = enteredPin.length > i
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError -> Color(0xFFEF4444)
                                        isFilled -> LufickEmerald
                                        else -> Color.White.copy(alpha = 0.25f)
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Numpad Keypad (1 to 9, Biometric, 0, Backspace)
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                keypad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (key) {
                                            "BIO" -> LufickEmerald.copy(alpha = 0.2f)
                                            "DEL" -> Color.White.copy(alpha = 0.1f)
                                            else -> Color(0xFF1E293B)
                                        }
                                    )
                                    .clickable {
                                        when (key) {
                                            "BIO" -> {
                                                if (isBiometricEnabled) {
                                                    onUnlockWithBiometric()
                                                }
                                            }
                                            "DEL" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    isError = false
                                                }
                                            }
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    val newPin = enteredPin + key
                                                    enteredPin = newPin
                                                    if (newPin.length == 4) {
                                                        val success = onUnlockWithPin(newPin)
                                                        if (!success) {
                                                            isError = true
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "BIO" -> {
                                        Icon(
                                            Icons.Default.Fingerprint,
                                            contentDescription = "Biometric Unlock",
                                            tint = LufickEmerald,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    "DEL" -> {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
