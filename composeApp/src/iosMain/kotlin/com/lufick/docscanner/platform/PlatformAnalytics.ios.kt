package com.lufick.docscanner.platform

actual class PlatformAnalytics {
    actual fun logEvent(name: String, params: Map<String, String>) {}
    actual fun setUserProperty(name: String, value: String) {}
    actual fun setScreen(screenName: String) {}
}

actual class PlatformCrashlytics {
    actual fun log(message: String) {}
    actual fun recordException(throwable: Throwable) {}
    actual fun setCustomKey(key: String, value: String) {}
}

private val iosAnalytics = PlatformAnalytics()
private val iosCrashlytics = PlatformCrashlytics()

actual fun getPlatformAnalytics(): PlatformAnalytics = iosAnalytics
actual fun getPlatformCrashlytics(): PlatformCrashlytics = iosCrashlytics
