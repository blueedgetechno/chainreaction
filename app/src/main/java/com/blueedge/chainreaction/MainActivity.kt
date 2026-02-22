package com.blueedge.chainreaction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.blueedge.chainreaction.audio.SoundManager
import com.blueedge.chainreaction.data.GameConfig
import com.blueedge.chainreaction.ui.navigation.ChainReactionNavGraph
import com.blueedge.chainreaction.ui.theme.ChainReactionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GameConfig.load(this)
        SoundManager.init(this)
        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            ChainReactionTheme {
                val navController = rememberNavController()
                ChainReactionNavGraph(navController = navController)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        SoundManager.pauseMusic()
        GameConfig.save(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}