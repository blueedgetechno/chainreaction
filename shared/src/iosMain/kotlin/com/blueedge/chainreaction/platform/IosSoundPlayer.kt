package com.blueedge.chainreaction.platform

import com.blueedge.chainreaction.data.GameConfig
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
object IosSoundPlayer : SoundPlayer {

    private var bopPlayer: AVAudioPlayer? = null
    private var popPlayer: AVAudioPlayer? = null
    private var musicPlayer: AVAudioPlayer? = null

    init {
        // Configure audio session for playback
        try {
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
            AVAudioSession.sharedInstance().setActive(true, null)
        } catch (_: Exception) {}

        bopPlayer = loadPlayer("bop", "caf")
        popPlayer = loadPlayer("pop", "caf")
    }

    private fun loadPlayer(name: String, ext: String): AVAudioPlayer? {
        val url = NSBundle.mainBundle.URLForResource(name, ext) ?: return null
        return try {
            AVAudioPlayer(contentsOfURL = url, error = null).apply {
                prepareToPlay()
            }
        } catch (_: Exception) { null }
    }

    override fun playBop() {
        if (!GameConfig.soundEnabled) return
        bopPlayer?.let {
            it.currentTime = 0.0
            it.play()
        }
    }

    override fun playPop() {
        if (!GameConfig.soundEnabled) return
        popPlayer?.let {
            it.currentTime = 0.0
            it.play()
        }
    }

    override fun vibrate(durationMs: Long) {
        // No vibration on simulator; could add UIImpactFeedbackGenerator for device
    }

    override fun startMusic() {
        if (musicPlayer != null) return
        val url = NSBundle.mainBundle.URLForResource("bgmusic", "caf") ?: return
        musicPlayer = try {
            AVAudioPlayer(contentsOfURL = url, error = null).apply {
                numberOfLoops = -1 // loop forever
                volume = 0.4f
                prepareToPlay()
                play()
            }
        } catch (_: Exception) { null }
    }

    override fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer = null
    }

    override fun pauseMusic() {
        musicPlayer?.pause()
    }

    override fun resumeMusic() {
        if (GameConfig.musicEnabled) {
            musicPlayer?.play() ?: startMusic()
        }
    }

    override fun onMusicToggled() {
        if (GameConfig.musicEnabled) {
            startMusic()
        } else {
            stopMusic()
        }
    }

    override fun release() {
        bopPlayer?.stop()
        popPlayer?.stop()
        bopPlayer = null
        popPlayer = null
        stopMusic()
    }
}
