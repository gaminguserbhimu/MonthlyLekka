package com.vinay.monthlylekka.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Singleton manager to handle loading and displaying Interstitial Ads.
 * 100% Ad-Free Offline Mode: all ad operations are no-ops.
 */
object InterstitialAdManager {

    /**
     * Preload an interstitial ad (no-op).
     */
    fun loadAd(@Suppress("UNUSED_PARAMETER") context: Context) {
        // No-op for 100% ad-free offline mode
    }

    /**
     * Preload an interstitial ad (no-op alias).
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    fun preloadInterstitialAd(context: Context) {
        // No-op for 100% ad-free offline mode
    }

    /**
     * Show the preloaded interstitial ad using the provided Activity (no-op).
     */
    fun showAd(@Suppress("UNUSED_PARAMETER") activity: Activity?) {
        // No-op for 100% ad-free offline mode
    }

    /**
     * Show the preloaded interstitial ad using the provided Activity (no-op alias).
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    fun showInterstitialAd(activity: Activity?) {
        // No-op for 100% ad-free offline mode
    }
}

/**
 * Extension function to extract an Activity from a Context.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
