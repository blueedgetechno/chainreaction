package com.blueedge.chainreaction.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CompletableDeferred

/**
 * Singleton manager for loading and showing AdMob interstitial ads.
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"

    // TODO: Replace with production ad unit ID for release builds:
    private const val AD_UNIT_ID = "ca-app-pub-8183419630789011/3585233501"
    // private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Google test interstitial ad

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    /** Signals when MobileAds SDK initialization has finished. */
    private val sdkInitialized = CompletableDeferred<Boolean>()

    /**
     * Initialize the Mobile Ads SDK. Call once from Activity.onCreate.
     */
    fun initializeSdk(context: Context) {
        MobileAds.initialize(context) {
            Log.d(TAG, "MobileAds SDK initialized.")
            sdkInitialized.complete(true)
        }
    }

    /**
     * Preload an interstitial ad. Suspends until the SDK is initialized,
     * then fires the ad load request.
     */
    suspend fun load(context: Context) {
        // Wait for SDK to be ready before requesting an ad
        sdkInitialized.await()

        if (interstitialAd != null || isLoading) return

        isLoading = true
        Log.d(TAG, "Requesting interstitial ad...")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: code=${adError.code}, message=${adError.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /** @return true if an ad is loaded and ready to show. */
    fun isReady(): Boolean = interstitialAd != null

    /**
     * Show the interstitial ad if one is loaded.
     *
     * @param activity The Activity context required to display the ad.
     * @param onAdDismissed Called when the ad is dismissed (or if no ad was available).
     */
    fun show(activity: Activity, onAdDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not ready, skipping.")
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed.")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad shown.")
            }
        }

        ad.show(activity)
    }
}
