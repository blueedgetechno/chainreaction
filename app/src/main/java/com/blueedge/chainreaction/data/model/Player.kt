package com.blueedge.chainreaction.data.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a player in the game
 */
data class Player(
    val id: Int,
    val name: String,
    val color: Color
) {
    companion object {
        fun create(id: Int, name: String, color: Color) = Player(id, name, color)
    }
}

enum class PlayerType {
    HUMAN,
    BOT
}
