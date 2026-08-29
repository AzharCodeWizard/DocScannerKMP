package com.lufick.docscanner.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LocalImage(path: String, modifier: Modifier = Modifier)
