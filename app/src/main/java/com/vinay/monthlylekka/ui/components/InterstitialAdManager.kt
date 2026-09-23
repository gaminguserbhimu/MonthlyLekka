package com.vinay.monthlylekka.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Singleton manager to handle loading and displaying Google Mobile Ads Interstitial Ads.
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"
    const val AD_UNIT_ID = "ca-app-pub-4120760179761356/1771535176"

    private var mInterstitialAd: InterstitialAd? = null
    private var isLoading = false

    /**
     * Preload an interstitial ad using Google Mobile Ads SDK.
     */
    fun loadAd(context: Context, adUnitId: String = AD_UNIT_ID) {
        if ((mInterstitialAd != null) || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    isLoading = false
                    Log.e(TAG, "Failed to load interstitial ad: ${loadAdError.message}")
                }
            },
        )
    }

    /**
     * Preload an interstitial ad (alias).
     */
    @Suppress("unused")
    fun preloadInterstitialAd(context: Context) {
        loadAd(context)
    }

    /**
     * Show the preloaded interstitial ad using the provided Activity.
     */
    fun showAd(activity: Activity?) {
        if (activity == null) return

        val ad = mInterstitialAd
        if (ad != null) {
            mInterstitialAd = null
            ad.show(activity)
            loadAd(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready yet.")
            loadAd(activity)
        }
    }

    /**
     * Show the preloaded interstitial ad using the provided Activity (alias).
     */
    @Suppress("unused")
    fun showInterstitialAd(activity: Activity?) {
        showAd(activity)
    }
}

/**
 * Extension function to extract an Activity from a Context.
 */
fun Context.findActivity(): Activity? {
    var context = this
    while ((context !is Activity) && (context is ContextWrapper)) {
        context = context.baseContext
    }
    return context as? Activity
}
