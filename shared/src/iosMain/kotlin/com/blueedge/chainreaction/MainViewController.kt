package com.blueedge.chainreaction

import androidx.compose.ui.window.ComposeUIViewController
import com.blueedge.chainreaction.platform.IosSoundPlayer
import com.blueedge.chainreaction.platform.ServiceLocator

fun MainViewController() = ComposeUIViewController {
    ServiceLocator.soundPlayer = IosSoundPlayer
    App(soundPlayer = IosSoundPlayer)
}
