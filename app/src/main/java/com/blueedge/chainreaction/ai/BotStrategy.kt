package com.blueedge.chainreaction.ai

import com.blueedge.chainreaction.data.model.BoardState
import com.blueedge.chainreaction.data.model.Move
import com.blueedge.chainreaction.data.model.Player

/**
 * Interface for bot strategies
 */
interface BotStrategy {
    suspend fun calculateMove(
        boardState: BoardState,
        botPlayer: Player,
        opponentPlayer: Player
    ): Move?
}
