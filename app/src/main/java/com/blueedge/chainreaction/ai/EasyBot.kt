package com.blueedge.chainreaction.ai

import com.blueedge.chainreaction.data.model.BoardState
import com.blueedge.chainreaction.data.model.Move
import com.blueedge.chainreaction.data.model.Player
import com.blueedge.chainreaction.domain.GameEngine
import javax.inject.Inject

/**
 * Easy bot that makes random valid moves
 */
class EasyBot @Inject constructor(
    private val gameEngine: GameEngine
) : BotStrategy {
    
    override suspend fun calculateMove(
        boardState: BoardState,
        botPlayer: Player,
        opponentPlayer: Player
    ): Move? {
        val validMoves = gameEngine.getValidMoves(boardState, botPlayer)
        return validMoves.randomOrNull()
    }
}
