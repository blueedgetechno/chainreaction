package com.blueedge.chainreaction.platform

/** Abstraction for interstitial ad management. */
interface AdManager {
    suspend fun load()
    fun isReady(): Boolean
    fun show(onAdDismissed: () -> Unit = {})
}

/** No-op implementation for platforms without ads (iOS initially). */
object NoOpAdManager : AdManager {
    override suspend fun load() {}
    override fun isReady(): Boolean = false
    override fun show(onAdDismissed: () -> Unit) { onAdDismissed() }
}
