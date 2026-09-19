package com.vinay.monthlylekka.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Singleton manager to handle loading and displaying Interstitial Ads.
 * Configured cleanly for Amazon Appstore release distribution.
 */
object InterstitialAdManager {

    /**
     * Preload an interstitial ad for Amazon Appstore release distribution.
     */
    fun loadAd(@Suppress("UNUSED_PARAMETER") context: Context) {
        // Configured cleanly for Amazon Appstore release distribution
    }

    /**
     * Preload an interstitial ad (alias) for Amazon Appstore release distribution.
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    fun preloadInterstitialAd(context: Context) {
        // Configured cleanly for Amazon Appstore release distribution
    }

    /**
     * Show the preloaded interstitial ad using the provided Activity for Amazon Appstore release distribution.
     */
    fun showAd(@Suppress("UNUSED_PARAMETER") activity: Activity?) {
        // Configured cleanly for Amazon Appstore release distribution
    }

    /**
     * Show the preloaded interstitial ad using the provided Activity (alias) for Amazon Appstore release distribution.
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    fun showInterstitialAd(activity: Activity?) {
        // Configured cleanly for Amazon Appstore release distribution
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
