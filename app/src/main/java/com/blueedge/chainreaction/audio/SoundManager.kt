package com.blueedge.chainreaction.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.blueedge.chainreaction.R
import com.blueedge.chainreaction.data.GameConfig

/**
 * Singleton that manages game sound effects (SoundPool), background music (MediaPlayer),
 * and haptic feedback (Vibrator).
 * Call [init] once from Activity.onCreate, then use the play/vibrate helpers anywhere.
 */
object SoundManager {

    private var soundPool: SoundPool? = null
    private var bopSoundId: Int = 0
    private var popSoundId: Int = 0
    private var loaded = false

    // Background music
    private var mediaPlayer: MediaPlayer? = null
    private var appContext: Context? = null

    // Vibration
    private var vibrator: Vibrator? = null

    fun init(context: Context) {
        appContext = context.applicationContext

        // ---------- SoundPool (SFX) ----------
        if (soundPool == null) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attrs)
                .build()
                .also { pool ->
                    pool.setOnLoadCompleteListener { _, _, status ->
                        if (status == 0) loaded = true
                    }

                    // Sound from Pixabay: @KoiRoylers
                    bopSoundId = pool.load(context, R.raw.bop, 1)
                    popSoundId = pool.load(context, R.raw.pop, 1)
                }
        }

        // ---------- Vibrator ----------
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // ---------- Background music ----------
        if (GameConfig.musicEnabled) {
            startMusic()
        }
    }

    // ==================== Background Music ====================

    fun startMusic() {
        if (mediaPlayer != null) return
        val ctx = appContext ?: return
        mediaPlayer = MediaPlayer.create(ctx, R.raw.bgmusic)?.apply {
            isLooping = true
            setVolume(0.4f, 0.4f)
            start()
        }
    }

    fun stopMusic() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    /** Pause music when the app goes to background. */
    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    /** Resume music when the app comes back to foreground (respects toggle). */
    fun resumeMusic() {
        if (GameConfig.musicEnabled) {
            mediaPlayer?.start() ?: startMusic()
        }
    }

    /** Call when the user toggles the music switch in settings. */
    fun onMusicToggled() {
        if (GameConfig.musicEnabled) {
            startMusic()
        } else {
            stopMusic()
        }
    }

    // ==================== SFX ====================

    /** Play the "bop" sound (button / cell tap). Respects [GameConfig.soundEnabled]. */
    fun playBop() {
        if (!GameConfig.soundEnabled) return
        soundPool?.play(bopSoundId, 1f, 1f, 1, 0, 1f)
    }

    /** Play the "pop" sound (cell split / explosion). Respects [GameConfig.soundEnabled]. */
    fun playPop() {
        if (!GameConfig.soundEnabled) return
        soundPool?.play(popSoundId, 1f, 1f, 1, 0, 1f)
    }

    // ==================== Vibration ====================

    /** Short vibration pulse for cell splits. Respects [GameConfig.vibrationEnabled]. */
    fun vibrate(durationMs: Long = 50) {
        if (!GameConfig.vibrationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMs)
        }
    }

    // ==================== Lifecycle ====================

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded = false
        stopMusic()
    }
}
