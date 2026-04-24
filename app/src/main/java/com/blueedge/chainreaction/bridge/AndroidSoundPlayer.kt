package com.blueedge.chainreaction.bridge

import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.platform.SoundPlayer

/** Bridges the Android SoundManager singleton to the shared SoundPlayer interface. */
object AndroidSoundPlayer : SoundPlayer {
    override fun playBop() = SoundManager.playBop()
    override fun playPop() = SoundManager.playPop()
    override fun vibrate(durationMs: Long) = SoundManager.vibrate(durationMs)
    override fun startMusic() = SoundManager.startMusic()
    override fun stopMusic() = SoundManager.stopMusic()
    override fun pauseMusic() = SoundManager.pauseMusic()
    override fun resumeMusic() = SoundManager.resumeMusic()
    override fun onMusicToggled() = SoundManager.onMusicToggled()
    override fun release() = SoundManager.release()
}
