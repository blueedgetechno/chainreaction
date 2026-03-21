package com.blueedge.chainreaction.platform

/** Abstraction for game sound effects, background music, and haptic feedback. */
interface SoundPlayer {
    fun playBop()
    fun playPop()
    fun vibrate(durationMs: Long = 50)
    fun startMusic()
    fun stopMusic()
    fun pauseMusic()
    fun resumeMusic()
    fun onMusicToggled()
    fun release()
}

/** No-op implementation used as default and on iOS until real audio is wired. */
object NoOpSoundPlayer : SoundPlayer {
    override fun playBop() {}
    override fun playPop() {}
    override fun vibrate(durationMs: Long) {}
    override fun startMusic() {}
    override fun stopMusic() {}
    override fun pauseMusic() {}
    override fun resumeMusic() {}
    override fun onMusicToggled() {}
    override fun release() {}
}
