package com.vinay.monthlylekka.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Singleton manager to handle loading and displaying AdMob Interstitial Ads.
 */
object InterstitialAdManager {
    private const val TAG = "InterstitialAdManager"
    private const val AD_UNIT_ID = "ca-app-pub-4120760179761356/1771535176"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    /**
     * Preload an interstitial ad.
     */
    fun loadAd(context: Context) {
        if (interstitialAd != null || isLoading) {
            return
        }
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Show the preloaded interstitial ad using the provided Activity.
     */
    fun showAd(activity: Activity?) {
        if (activity == null) {
            Log.d(TAG, "Activity context is null. Cannot show interstitial ad.")
            return
        }
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed fullscreen content.")
                    interstitialAd = null
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadAd(activity)
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad was not loaded yet. Requesting load.")
            loadAd(activity)
        }
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
