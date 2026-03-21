package com.blueedge.chainreaction.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** Provides platform-specific implementations to Compose UI via CompositionLocal. */
val LocalSoundPlayer = staticCompositionLocalOf<SoundPlayer> { NoOpSoundPlayer }
val LocalAdManager = staticCompositionLocalOf<AdManager> { NoOpAdManager }
val LocalOnlineGameRepo = staticCompositionLocalOf<OnlineGameRepo> { NoOpOnlineGameRepo }
