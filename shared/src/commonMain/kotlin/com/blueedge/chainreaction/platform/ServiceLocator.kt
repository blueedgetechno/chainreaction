package com.blueedge.chainreaction.platform

/**
 * Singleton holder for platform services, set at app startup.
 * Used by ViewModels and other non-Composable code that can't use CompositionLocal.
 */
object ServiceLocator {
    var soundPlayer: SoundPlayer = NoOpSoundPlayer
    var adManager: AdManager = NoOpAdManager
}
