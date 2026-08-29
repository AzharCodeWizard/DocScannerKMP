package com.lufick.docscanner.util

import kotlinx.datetime.Clock

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
