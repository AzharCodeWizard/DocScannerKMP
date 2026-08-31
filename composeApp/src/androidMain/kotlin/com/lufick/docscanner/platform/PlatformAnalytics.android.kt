package com.lufick.docscanner.platform

import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

private const val TAG = "DocScannerAnalytics"

actual class PlatformAnalytics {
    private val analytics: FirebaseAnalytics? by lazy {
        try {
            val app = FirebaseApp.getInstance()
            FirebaseAnalytics.getInstance(app.applicationContext)
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase Analytics offline/uninitialized", e)
            null
        }
    }

    actual fun logEvent(name: String, params: Map<String, String>) {
        try {
            val bundle = Bundle().apply {
                params.forEach { (k, v) -> putString(k, v) }
            }
            analytics?.logEvent(name, bundle)
            Log.d(TAG, "Analytics Event: $name $params")
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to log event $name", e)
        }
    }

    actual fun setUserProperty(name: String, value: String) {
        try {
            analytics?.setUserProperty(name, value)
        } catch (_: Throwable) {}
    }

    actual fun setScreen(screenName: String) {
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, mapOf(FirebaseAnalytics.Param.SCREEN_NAME to screenName))
    }
}

actual class PlatformCrashlytics {
    private val crashlytics: FirebaseCrashlytics? by lazy {
        try {
            FirebaseCrashlytics.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase Crashlytics offline/uninitialized", e)
            null
        }
    }

    actual fun log(message: String) {
        try {
            crashlytics?.log(message)
            Log.d(TAG, "Crashlytics Log: $message")
        } catch (_: Throwable) {}
    }

    actual fun recordException(throwable: Throwable) {
        try {
            crashlytics?.recordException(throwable)
            Log.e(TAG, "Crashlytics Recorded Exception", throwable)
        } catch (_: Throwable) {}
    }

    actual fun setCustomKey(key: String, value: String) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (_: Throwable) {}
    }
}

private val androidAnalytics = PlatformAnalytics()
private val androidCrashlytics = PlatformCrashlytics()

actual fun getPlatformAnalytics(): PlatformAnalytics = androidAnalytics
actual fun getPlatformCrashlytics(): PlatformCrashlytics = androidCrashlytics
