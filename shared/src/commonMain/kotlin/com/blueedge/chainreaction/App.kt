package com.blueedge.chainreaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import com.blueedge.chainreaction.platform.AdManager
import com.blueedge.chainreaction.platform.LocalAdManager
import com.blueedge.chainreaction.platform.LocalSoundPlayer
import com.blueedge.chainreaction.platform.LocalOnlineGameRepo
import com.blueedge.chainreaction.platform.NoOpAdManager
import com.blueedge.chainreaction.platform.NoOpSoundPlayer
import com.blueedge.chainreaction.platform.NoOpOnlineGameRepo
import com.blueedge.chainreaction.platform.OnlineGameRepo
import com.blueedge.chainreaction.platform.SoundPlayer
import com.blueedge.chainreaction.ui.navigation.ChainReactionNavGraph
import com.blueedge.chainreaction.ui.theme.ChainReactionTheme

@Composable
fun App(
    soundPlayer: SoundPlayer = NoOpSoundPlayer,
    adManager: AdManager = NoOpAdManager,
    onlineGameRepo: OnlineGameRepo = NoOpOnlineGameRepo
) {
    CompositionLocalProvider(
        LocalSoundPlayer provides soundPlayer,
        LocalAdManager provides adManager,
        LocalOnlineGameRepo provides onlineGameRepo
    ) {
        ChainReactionTheme {
            val navController = rememberNavController()
            ChainReactionNavGraph(navController)
        }
    }
}
