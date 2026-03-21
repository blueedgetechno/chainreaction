package com.blueedge.chainreaction.ai

import com.blueedge.chainreaction.data.BotDifficulty
import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.GameVariant
import com.blueedge.chainreaction.data.Move
import com.blueedge.chainreaction.domain.GameEngine
import kotlinx.coroutines.delay
import kotlin.random.Random

interface BotStrategy {
    suspend fun calculateMove(
        board: List<List<CellState>>,
        botPlayerId: Int,
        opponentId: Int,
        isFirstMove: Boolean = false
    ): Move?
}

abstract class MinimaxBot(
    variant: GameVariant,
    private val searchDepth: Int,
    private val thinkingDelay: Long,
    private val randomMoveChance: Double = 0.0,
    private val maxCandidates: Int = 8
) : BotStrategy {
    protected val engine = GameEngine(variant)

    override suspend fun calculateMove(
        board: List<List<CellState>>,
        botPlayerId: Int,
        opponentId: Int,
        isFirstMove: Boolean
    ): Move? {
        delay(thinkingDelay)
        val validMoves = engine.getValidMoves(board, botPlayerId, isFirstMove)
        if (validMoves.isEmpty()) return null

        if (isFirstMove) return validMoves.random()

        if (randomMoveChance > 0.0 && Random.nextDouble() < randomMoveChance) {
            return validMoves.random()
        }

        var bestScore = Int.MIN_VALUE
        val bestMoves = mutableListOf<Move>()

        for (move in validMoves) {
            val (newBoard, _) = engine.executeMove(board, move.row, move.col, botPlayerId, isFirstMove)

            val winner = engine.checkWinCondition(newBoard, 100)
            if (winner == botPlayerId) return move

            val score = minimax(newBoard, searchDepth, false, botPlayerId, opponentId, Int.MIN_VALUE, Int.MAX_VALUE)
            if (score > bestScore) {
                bestScore = score
                bestMoves.clear()
                bestMoves.add(move)
            } else if (score == bestScore) {
                bestMoves.add(move)
            }
        }

        return bestMoves.random()
    }

    private fun minimax(
        board: List<List<CellState>>,
        depth: Int,
        isMaximizing: Boolean,
        botPlayerId: Int,
        opponentId: Int,
        alpha: Int,
        beta: Int
    ): Int {
        val winner = engine.checkWinCondition(board, 100)
        if (winner == botPlayerId) return 10000 + depth
        if (winner == opponentId) return -10000 - depth
        if (depth == 0) return evaluate(board, botPlayerId, opponentId)

        var currentAlpha = alpha
        var currentBeta = beta

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            val moves = engine.getValidMoves(board, botPlayerId)
            if (moves.isEmpty()) return evaluate(board, botPlayerId, opponentId)

            val candidates = prioritizeMoves(moves, board, botPlayerId).take(maxCandidates)
            for (move in candidates) {
                val (newBoard, _) = engine.executeMove(board, move.row, move.col, botPlayerId)
                val eval = minimax(newBoard, depth - 1, false, botPlayerId, opponentId, currentAlpha, currentBeta)
                maxEval = maxOf(maxEval, eval)
                currentAlpha = maxOf(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            val moves = engine.getValidMoves(board, opponentId)
            if (moves.isEmpty()) return evaluate(board, botPlayerId, opponentId)

            val candidates = prioritizeMoves(moves, board, opponentId).take(maxCandidates)
            for (move in candidates) {
                val (newBoard, _) = engine.executeMove(board, move.row, move.col, opponentId)
                val eval = minimax(newBoard, depth - 1, true, botPlayerId, opponentId, currentAlpha, currentBeta)
                minEval = minOf(minEval, eval)
                currentBeta = minOf(currentBeta, eval)
                if (currentBeta <= currentAlpha) break
            }
            return minEval
        }
    }

    private fun prioritizeMoves(
        moves: List<Move>,
        board: List<List<CellState>>,
        playerId: Int
    ): List<Move> {
        return moves.sortedByDescending { move ->
            val cell = board[move.row][move.col]
            val critMass = engine.getCriticalMass(move.row, move.col, board.size, board[0].size)
            var priority = 0
            if (cell.ownerId == playerId && cell.dots == critMass - 1) {
                priority += 100
            }
            if (cell.ownerId == playerId) {
                priority += cell.dots * 10
            }
            priority
        }
    }

    private fun evaluate(board: List<List<CellState>>, botPlayerId: Int, opponentId: Int): Int {
        var score = 0
        val gridRows = board.size
        val gridCols = board[0].size

        for (r in board.indices) {
            for (c in board[r].indices) {
                val cell = board[r][c]
                when (cell.ownerId) {
                    botPlayerId -> {
                        score += 10
                        score += cell.dots * 3
                        val critMass = engine.getCriticalMass(r, c, gridRows, gridCols)
                        if (cell.dots == critMass - 1) {
                            val neighbors = getNeighbors(r, c, gridRows, gridCols)
                            val opponentNeighbors = neighbors.count { board[it.row][it.col].ownerId == opponentId }
                            score += opponentNeighbors * 8
                        }
                        val neighborCount = getNeighbors(r, c, gridRows, gridCols).size
                        if (neighborCount == 2) score += 5
                        else if (neighborCount == 3) score += 3
                    }
                    opponentId -> {
                        score -= 10
                        score -= cell.dots * 3
                        val critMass = engine.getCriticalMass(r, c, gridRows, gridCols)
                        if (cell.dots == critMass - 1) {
                            val neighbors = getNeighbors(r, c, gridRows, gridCols)
                            val botNeighbors = neighbors.count { board[it.row][it.col].ownerId == botPlayerId }
                            score -= botNeighbors * 8
                        }
                        val neighborCount = getNeighbors(r, c, gridRows, gridCols).size
                        if (neighborCount == 2) score -= 5
                        else if (neighborCount == 3) score -= 3
                    }
                }
            }
        }

        return score
    }

    private fun getNeighbors(row: Int, col: Int, rows: Int, cols: Int): List<Move> {
        val neighbors = mutableListOf<Move>()
        if (row > 0) neighbors.add(Move(row - 1, col))
        if (row < rows - 1) neighbors.add(Move(row + 1, col))
        if (col > 0) neighbors.add(Move(row, col - 1))
        if (col < cols - 1) neighbors.add(Move(row, col + 1))
        return neighbors
    }
}

class EasyBot(variant: GameVariant) : MinimaxBot(variant, searchDepth = 0, thinkingDelay = 800, randomMoveChance = 0.75)

class MediumBot(variant: GameVariant) : MinimaxBot(variant, searchDepth = 0, thinkingDelay = 1200)

class HardBot(variant: GameVariant) : MinimaxBot(variant, searchDepth = 1, thinkingDelay = 1500)

fun createBot(difficulty: BotDifficulty, variant: GameVariant = GameVariant.SIMPLE): BotStrategy {
    return when (difficulty) {
        BotDifficulty.EASY -> EasyBot(variant)
        BotDifficulty.MEDIUM -> MediumBot(variant)
        BotDifficulty.HARD -> HardBot(variant)
    }
}
