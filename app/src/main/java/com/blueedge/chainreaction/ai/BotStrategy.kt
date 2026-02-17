package com.blueedge.chainreaction.ai

import com.blueedge.chainreaction.data.BotDifficulty
import com.blueedge.chainreaction.data.CellState
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
    ): Move
}

class EasyBot : BotStrategy {
    private val engine = GameEngine()

    override suspend fun calculateMove(
        board: List<List<CellState>>,
        botPlayerId: Int,
        opponentId: Int,
        isFirstMove: Boolean
    ): Move {
        delay(800)
        val validMoves = engine.getValidMoves(board, botPlayerId, isFirstMove)
        return validMoves[Random.nextInt(validMoves.size)]
    }
}

class MediumBot : BotStrategy {
    private val engine = GameEngine()

    override suspend fun calculateMove(
        board: List<List<CellState>>,
        botPlayerId: Int,
        opponentId: Int,
        isFirstMove: Boolean
    ): Move {
        delay(1200)
        val validMoves = engine.getValidMoves(board, botPlayerId, isFirstMove)
        val gridRows = board.size
        val gridCols = board[0].size

        // If first move, pick a random empty cell
        if (isFirstMove) return validMoves.random()

        // Priority 1: Moves that cause explosions capturing opponent cells
        val explosionCaptures = validMoves.filter { move ->
            val cell = board[move.row][move.col]
            if (cell.ownerId == botPlayerId && cell.dots == GameEngine.CRITICAL_MASS - 1) {
                val neighbors = getNeighbors(move.row, move.col, gridRows, gridCols)
                neighbors.any { n -> board[n.row][n.col].ownerId == opponentId }
            } else false
        }
        if (explosionCaptures.isNotEmpty()) return explosionCaptures.random()

        // Priority 2: Block opponent cells about to explode
        val blockingMoves = validMoves.filter { move ->
            val neighbors = getNeighbors(move.row, move.col, gridRows, gridCols)
            neighbors.any { n ->
                board[n.row][n.col].ownerId == opponentId &&
                        board[n.row][n.col].dots == GameEngine.CRITICAL_MASS - 1
            }
        }.filter { move ->
            board[move.row][move.col].ownerId == botPlayerId
        }
        if (blockingMoves.isNotEmpty()) return blockingMoves.random()

        // Priority 3: Build up own cells (prefer cells with more dots)
        val buildMoves = validMoves.filter { move ->
            board[move.row][move.col].ownerId == botPlayerId
        }.sortedByDescending { board[it.row][it.col].dots }
        if (buildMoves.isNotEmpty()) return buildMoves.first()

        return validMoves.random()
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

class HardBot : BotStrategy {
    private val engine = GameEngine()

    override suspend fun calculateMove(
        board: List<List<CellState>>,
        botPlayerId: Int,
        opponentId: Int,
        isFirstMove: Boolean
    ): Move {
        delay(1500)
        val validMoves = engine.getValidMoves(board, botPlayerId, isFirstMove)

        // If first move, pick a random empty cell
        if (isFirstMove) return validMoves.random()

        var bestScore = Int.MIN_VALUE
        var bestMove = validMoves.first()

        for (move in validMoves) {
            val (newBoard, _) = engine.executeMove(board, move.row, move.col, botPlayerId, isFirstMove)

            // Check for immediate win
            val winner = engine.checkWinCondition(newBoard, 100)
            if (winner == botPlayerId) return move

            val score = minimax(newBoard, 3, false, botPlayerId, opponentId, Int.MIN_VALUE, Int.MAX_VALUE)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
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

            // Limit branching factor for performance
            val candidates = prioritizeMoves(moves, board, botPlayerId).take(8)
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

            val candidates = prioritizeMoves(moves, board, opponentId).take(8)
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
            var priority = 0
            if (cell.ownerId == playerId && cell.dots == GameEngine.CRITICAL_MASS - 1) {
                priority += 100 // About to explode
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
                        score += 10 // Territory
                        score += cell.dots * 3 // Dot count
                        // Bonus for cells about to explode near opponent
                        if (cell.dots == GameEngine.CRITICAL_MASS - 1) {
                            val neighbors = getNeighbors(r, c, gridRows, gridCols)
                            val opponentNeighbors = neighbors.count { board[it.row][it.col].ownerId == opponentId }
                            score += opponentNeighbors * 8
                        }
                        // Corner and edge bonus (harder to attack)
                        val neighborCount = getNeighbors(r, c, gridRows, gridCols).size
                        if (neighborCount == 2) score += 5 // Corner
                        else if (neighborCount == 3) score += 3 // Edge
                    }
                    opponentId -> {
                        score -= 10
                        score -= cell.dots * 3
                        if (cell.dots == GameEngine.CRITICAL_MASS - 1) {
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

fun createBot(difficulty: BotDifficulty): BotStrategy {
    return when (difficulty) {
        BotDifficulty.EASY -> EasyBot()
        BotDifficulty.MEDIUM -> MediumBot()
        BotDifficulty.HARD -> HardBot()
    }
}
