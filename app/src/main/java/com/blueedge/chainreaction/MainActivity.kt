package com.blueedge.chainreaction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        setContent {
            ChainReactionTheme {
                val navController = rememberNavController()
                ChainReactionNavGraph(navController = navController)
            }
        }
    }
}