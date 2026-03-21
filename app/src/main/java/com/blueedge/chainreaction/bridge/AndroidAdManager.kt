package com.blueedge.chainreaction.bridge

import android.app.Activity
import com.blueedge.chainreaction.ads.InterstitialAdManager
import com.blueedge.chainreaction.platform.AdManager

/** Bridges the Android InterstitialAdManager singleton to the shared AdManager interface. */
class AndroidAdManager(private val activity: Activity) : AdManager {
    override suspend fun load() {
        InterstitialAdManager.load(activity)
    }

    override fun isReady(): Boolean = InterstitialAdManager.isReady()

    override fun show(onAdDismissed: () -> Unit) {
        InterstitialAdManager.show(activity, onAdDismissed)
    }
}
