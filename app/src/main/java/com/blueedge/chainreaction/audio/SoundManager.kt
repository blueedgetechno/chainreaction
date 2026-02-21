package com.blueedge.chainreaction.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.blueedge.chainreaction.R
import com.blueedge.chainreaction.data.GameConfig

/**
 * Singleton that manages game sound effects using SoundPool.
 * Call [init] once from Activity.onCreate, then use [playBop] / [playPop] anywhere.
 */
object SoundManager {

    private var soundPool: SoundPool? = null
    private var bopSoundId: Int = 0
    private var popSoundId: Int = 0
    private var loaded = false

    fun init(context: Context) {
        if (soundPool != null) return // already initialised

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)  // allow several overlapping pops during chain reactions
            .setAudioAttributes(attrs)
            .build()
            .also { pool ->
                pool.setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) loaded = true
                }
                bopSoundId = pool.load(context, R.raw.bop, 1)
                popSoundId = pool.load(context, R.raw.pop, 1)
            }
    }

    /** Play the "bop" sound (cell tap). Respects [GameConfig.soundEnabled]. */
    fun playBop() {
        if (!GameConfig.soundEnabled) return
        soundPool?.play(bopSoundId, 1f, 1f, 1, 0, 1f)
    }

    /** Play the "pop" sound (cell split / explosion). Respects [GameConfig.soundEnabled]. */
    fun playPop() {
        if (!GameConfig.soundEnabled) return
        soundPool?.play(popSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded = false
    }
}
