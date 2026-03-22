package com.blueedge.chainreaction

import androidx.compose.ui.window.ComposeUIViewController
import com.blueedge.chainreaction.platform.FirebaseBridge
import com.blueedge.chainreaction.platform.IosSoundPlayer
import com.blueedge.chainreaction.platform.IosOnlineGameRepo
import com.blueedge.chainreaction.platform.ServiceLocator

fun MainViewController() = MainViewController(firebaseBridge = null)

fun MainViewController(firebaseBridge: FirebaseBridge?) = ComposeUIViewController {
    ServiceLocator.soundPlayer = IosSoundPlayer

    val onlineGameRepo = if (firebaseBridge != null) {
        IosOnlineGameRepo(firebaseBridge)
    } else {
        com.blueedge.chainreaction.platform.NoOpOnlineGameRepo
    }
    ServiceLocator.onlineGameRepo = onlineGameRepo

    App(soundPlayer = IosSoundPlayer, onlineGameRepo = onlineGameRepo)
}
