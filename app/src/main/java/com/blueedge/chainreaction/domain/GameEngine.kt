package com.blueedge.chainreaction.domain

import com.blueedge.chainreaction.data.CellState
import com.blueedge.chainreaction.data.ExplosionMove
import com.blueedge.chainreaction.data.ExplosionWaveData
import com.blueedge.chainreaction.data.Move

class GameEngine {

    companion object {
        const val CRITICAL_MASS = 4
    }

    fun isValidMove(
        board: List<List<CellState>>,
        row: Int,
        col: Int,
        playerId: Int,
        isFirstMove: Boolean = false
    ): Boolean {
        if (row < 0 || row >= board.size || col < 0 || col >= board[0].size) return false
        val cell = board[row][col]
        return if (isFirstMove) {
            cell.isEmpty
        } else {
            cell.ownerId == playerId
        }
    }

    fun executeMove(
        board: List<List<CellState>>,
        row: Int,
        col: Int,
        playerId: Int,
        isFirstMove: Boolean = false
    ): Pair<List<List<CellState>>, List<ExplosionWaveData>> {
        val mutableBoard = board.map { it.toMutableList() }.toMutableList()

        if (isFirstMove) {
            // First move places 3 dots
            mutableBoard[row][col] = CellState(ownerId = playerId, dots = 3, previousDots = 0)
        } else {
            val currentCell = mutableBoard[row][col]
            mutableBoard[row][col] = CellState(ownerId = playerId, dots = currentCell.dots + 1, previousDots = currentCell.dots)
        }

        val allWaveData = mutableListOf<ExplosionWaveData>()
        processExplosions(mutableBoard, playerId, allWaveData)

        return Pair(mutableBoard.map { it.toList() }, allWaveData)
    }

    private fun processExplosions(
        board: MutableList<MutableList<CellState>>,
        playerId: Int,
        allWaveData: MutableList<ExplosionWaveData>
    ) {
        val maxIterations = board.size * board[0].size * 4
        var iterations = 0

        while (iterations < maxIterations) {
            iterations++

            val explodingCells = mutableListOf<Move>()
            for (r in board.indices) {
                for (c in board[r].indices) {
                    if (board[r][c].dots >= CRITICAL_MASS) {
                        explodingCells.add(Move(r, c))
                    }
                }
            }

            if (explodingCells.isEmpty()) break

            // Build explosion move data for animation
            val waveMoves = mutableListOf<ExplosionMove>()
            for (cell in explodingCells) {
                val neighbors = getNeighbors(cell.row, cell.col, board.size, board[0].size)
                for (neighbor in neighbors) {
                    waveMoves.add(
                        ExplosionMove(
                            fromRow = cell.row,
                            fromCol = cell.col,
                            toRow = neighbor.row,
                            toCol = neighbor.col,
                            playerId = playerId
                        )
                    )
                }
            }

            // Step 1: Empty all exploding cells
            for (cell in explodingCells) {
                board[cell.row][cell.col] = CellState(ownerId = 0, dots = 0)
            }

            // Snapshot board after emptying (before adding to neighbors)
            val boardBeforeSplit = board.map { it.toList() }

            // Step 2: Add dots to neighbors of each exploding cell (cap at 4)
            for (cell in explodingCells) {
                val neighbors = getNeighbors(cell.row, cell.col, board.size, board[0].size)
                for (neighbor in neighbors) {
                    val current = board[neighbor.row][neighbor.col]
                    board[neighbor.row][neighbor.col] = CellState(
                        ownerId = playerId,
                        dots = minOf(current.dots + 1, CRITICAL_MASS)
                    )
                }
            }

            // Snapshot board after adding to neighbors
            val boardAfterSplit = board.map { it.toList() }

            allWaveData.add(
                ExplosionWaveData(
                    explodingCells = explodingCells,
                    moves = waveMoves,
                    boardBeforeSplit = boardBeforeSplit,
                    boardAfterSplit = boardAfterSplit
                )
            )
        }
    }

    private fun getNeighbors(row: Int, col: Int, rows: Int, cols: Int): List<Move> {
        val neighbors = mutableListOf<Move>()
        if (row > 0) neighbors.add(Move(row - 1, col))
        if (row < rows - 1) neighbors.add(Move(row + 1, col))
        if (col > 0) neighbors.add(Move(row, col - 1))
        if (col < cols - 1) neighbors.add(Move(row, col + 1))
        return neighbors
    }

    fun checkWinCondition(board: List<List<CellState>>, moveCount: Int): Int? {
        if (moveCount < 2) return null

        val owners = mutableSetOf<Int>()
        var hasOccupied = false

        for (row in board) {
            for (cell in row) {
                if (!cell.isEmpty) {
                    hasOccupied = true
                    owners.add(cell.ownerId)
                }
            }
        }

        return if (hasOccupied && owners.size == 1) owners.first() else null
    }

    fun getValidMoves(board: List<List<CellState>>, playerId: Int, isFirstMove: Boolean = false): List<Move> {
        val moves = mutableListOf<Move>()
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (isValidMove(board, r, c, playerId, isFirstMove)) {
                    moves.add(Move(r, c))
                }
            }
        }
        return moves
    }

    fun countPlayerCells(board: List<List<CellState>>, playerId: Int): Int {
        return board.sumOf { row -> row.count { it.ownerId == playerId } }
    }

    fun countPlayerDots(board: List<List<CellState>>, playerId: Int): Int {
        return board.sumOf { row -> row.filter { it.ownerId == playerId }.sumOf { it.dots } }
    }

    fun createEmptyBoard(gridSize: Int): List<List<CellState>> {
        return List(gridSize) { List(gridSize) { CellState() } }
    }
}
