package com.blueedge.chainreaction.ai

import com.blueedge.chainreaction.data.model.BoardState
import com.blueedge.chainreaction.data.model.Move
import com.blueedge.chainreaction.data.model.Player
import com.blueedge.chainreaction.domain.GameEngine
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * Hard bot using minimax algorithm with alpha-beta pruning
 */
class HardBot @Inject constructor(
    private val gameEngine: GameEngine
) : BotStrategy {
    
    companion object {
        // Adaptive depth based on grid size for performance
        private fun getDepth(gridSize: Int): Int {
            return when {
                gridSize <= 6 -> 3
                gridSize <= 8 -> 2
                else -> 1  // 10x10 grid
            }
        }
        
        // Large positive/negative scores to represent definite win/loss states
        // High enough to dominate any positional evaluation, but not Int.MAX/MIN to avoid overflow
        private const val WIN_SCORE = 10000
        private const val LOSE_SCORE = -10000
    }
    
    override suspend fun calculateMove(
        boardState: BoardState,
        botPlayer: Player,
        opponentPlayer: Player
    ): Move? {
        val validMoves = gameEngine.getValidMoves(boardState, botPlayer)
        if (validMoves.isEmpty()) return null
        
        val depth = getDepth(boardState.gridSize)
        
        var bestMove: Move? = null
        var bestScore = Int.MIN_VALUE
        val alpha = Int.MIN_VALUE
        val beta = Int.MAX_VALUE
        
        for (move in validMoves) {
            val result = gameEngine.addDot(boardState, move.row, move.col, botPlayer)
            val score = minimax(
                result.newBoard,
                depth - 1,
                alpha,
                beta,
                false,
                botPlayer,
                opponentPlayer
            )
            
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        
        return bestMove
    }
    
    private fun minimax(
        boardState: BoardState,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        botPlayer: Player,
        opponentPlayer: Player
    ): Int {
        // Check terminal conditions
        val winner = gameEngine.checkWinCondition(boardState, listOf(botPlayer, opponentPlayer))
        if (winner != null) {
            return if (winner.id == botPlayer.id) WIN_SCORE else LOSE_SCORE
        }
        
        if (depth == 0) {
            return evaluateBoard(boardState, botPlayer, opponentPlayer)
        }
        
        var alphaLocal = alpha
        var betaLocal = beta
        
        if (isMaximizing) {
            // Bot's turn (maximize)
            var maxEval = Int.MIN_VALUE
            val moves = gameEngine.getValidMoves(boardState, botPlayer)
            
            for (move in moves) {
                val result = gameEngine.addDot(boardState, move.row, move.col, botPlayer)
                val eval = minimax(
                    result.newBoard,
                    depth - 1,
                    alphaLocal,
                    betaLocal,
                    false,
                    botPlayer,
                    opponentPlayer
                )
                maxEval = max(maxEval, eval)
                alphaLocal = max(alphaLocal, eval)
                if (betaLocal <= alphaLocal) break  // Beta cutoff
            }
            return maxEval
        } else {
            // Opponent's turn (minimize)
            var minEval = Int.MAX_VALUE
            val moves = gameEngine.getValidMoves(boardState, opponentPlayer)
            
            for (move in moves) {
                val result = gameEngine.addDot(boardState, move.row, move.col, opponentPlayer)
                val eval = minimax(
                    result.newBoard,
                    depth - 1,
                    alphaLocal,
                    betaLocal,
                    true,
                    botPlayer,
                    opponentPlayer
                )
                minEval = min(minEval, eval)
                betaLocal = min(betaLocal, eval)
                if (betaLocal <= alphaLocal) break  // Alpha cutoff
            }
            return minEval
        }
    }
    
    private fun evaluateBoard(
        boardState: BoardState,
        botPlayer: Player,
        opponentPlayer: Player
    ): Int {
        var score = 0
        
        // Territory control (cells owned)
        val botCells = boardState.getCellCount(botPlayer.id)
        val opponentCells = boardState.getCellCount(opponentPlayer.id)
        score += (botCells - opponentCells) * 10
        
        // Total dots (potential for explosions)
        val botDots = boardState.getTotalDots(botPlayer.id)
        val opponentDots = boardState.getTotalDots(opponentPlayer.id)
        score += (botDots - opponentDots) * 5
        
        // Cells close to explosion (threat/opportunity)
        for (row in 0 until boardState.gridSize) {
            for (col in 0 until boardState.gridSize) {
                val cell = boardState.getCell(row, col) ?: continue
                if (cell.isEmpty()) continue
                
                val criticalMass = getCriticalMass(boardState, row, col)
                val dotsNeeded = criticalMass - cell.dots
                
                if (cell.playerId == botPlayer.id) {
                    // Our cells close to explosion are good
                    score += (4 - dotsNeeded) * 3
                } else if (cell.playerId == opponentPlayer.id) {
                    // Opponent cells close to explosion are bad
                    score -= (4 - dotsNeeded) * 3
                }
            }
        }
        
        // Strategic positioning (corners and edges are valuable early)
        if (boardState.getOccupiedCells() < 15) {
            for (row in 0 until boardState.gridSize) {
                for (col in 0 until boardState.gridSize) {
                    val cell = boardState.getCell(row, col) ?: continue
                    if (cell.isEmpty()) continue
                    
                    val isCornerOrEdge = isCornerOrEdge(boardState, row, col)
                    if (isCornerOrEdge) {
                        if (cell.playerId == botPlayer.id) {
                            score += 2
                        } else {
                            score -= 2
                        }
                    }
                }
            }
        }
        
        return score
    }
    
    private fun getCriticalMass(board: BoardState, row: Int, col: Int): Int {
        val isCorner = (row == 0 || row == board.gridSize - 1) && 
                       (col == 0 || col == board.gridSize - 1)
        val isEdge = row == 0 || row == board.gridSize - 1 || 
                     col == 0 || col == board.gridSize - 1
        
        return when {
            isCorner -> 2
            isEdge -> 3
            else -> 4
        }
    }
    
    private fun isCornerOrEdge(board: BoardState, row: Int, col: Int): Boolean {
        return row == 0 || row == board.gridSize - 1 || 
               col == 0 || col == board.gridSize - 1
    }
}
