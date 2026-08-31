package com.lufick.docscanner.platform

expect class PlatformAnalytics {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun setUserProperty(name: String, value: String)
    fun setScreen(screenName: String)
}

expect class PlatformCrashlytics {
    fun log(message: String)
    fun recordException(throwable: Throwable)
    fun setCustomKey(key: String, value: String)
}

expect fun getPlatformAnalytics(): PlatformAnalytics
expect fun getPlatformCrashlytics(): PlatformCrashlytics
