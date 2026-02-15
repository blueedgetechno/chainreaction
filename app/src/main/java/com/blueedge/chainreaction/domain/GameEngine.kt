package com.blueedge.chainreaction.domain

import com.blueedge.chainreaction.data.model.*

/**
 * Core game engine that handles all game logic including explosions and chain reactions
 */
class GameEngine {
    
    /**
     * Checks if a move is valid for the given player
     */
    fun isValidMove(board: BoardState, row: Int, col: Int, player: Player): Boolean {
        val cell = board.getCell(row, col) ?: return false
        return cell.canAddDot(player)
    }
    
    /**
     * Adds a dot to the specified cell and handles all resulting explosions
     */
    fun addDot(
        board: BoardState,
        row: Int,
        col: Int,
        player: Player
    ): MoveResult {
        val cell = board.getCell(row, col) ?: throw IllegalArgumentException(
            "Invalid cell position: ($row, $col) on ${board.gridSize}x${board.gridSize} board"
        )
        
        if (!cell.canAddDot(player)) {
            throw IllegalStateException(
                "Cannot add dot: Player ${player.id} tried to add to cell ($row, $col) " +
                "owned by player ${cell.playerId}"
            )
        }
        
        // Add the dot
        val newCell = CellState(dots = cell.dots + 1, playerId = player.id)
        var currentBoard = board.updateCell(row, col, newCell)
        
        // Check if explosion should occur
        val explosions = mutableListOf<Explosion>()
        if (shouldExplode(currentBoard, row, col)) {
            val explosionResult = processExplosions(currentBoard, row, col, player.id)
            currentBoard = explosionResult.first
            explosions.addAll(explosionResult.second)
        }
        
        return MoveResult(currentBoard, explosions)
    }
    
    /**
     * Determines if a cell should explode based on its position and dot count
     */
    private fun shouldExplode(board: BoardState, row: Int, col: Int): Boolean {
        val cell = board.getCell(row, col) ?: return false
        val criticalMass = getCriticalMass(board, row, col)
        return cell.dots >= criticalMass
    }
    
    /**
     * Gets the critical mass (number of dots needed to explode) for a cell
     * Corner cells: 2, Edge cells: 3, Center cells: 4
     */
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
    
    /**
     * Processes explosions and chain reactions recursively
     */
    private fun processExplosions(
        board: BoardState,
        row: Int,
        col: Int,
        playerId: Int
    ): Pair<BoardState, List<Explosion>> {
        val explosions = mutableListOf<Explosion>()
        var currentBoard = board
        
        // Queue of cells to process
        val toProcess = mutableListOf(Triple(row, col, playerId))
        val processed = mutableSetOf<Pair<Int, Int>>()
        
        while (toProcess.isNotEmpty()) {
            val (r, c, pid) = toProcess.removeAt(0)
            val key = Pair(r, c)
            
            // Skip if already processed in this iteration
            if (key in processed) continue
            processed.add(key)
            
            val cell = currentBoard.getCell(r, c) ?: continue
            if (!shouldExplode(currentBoard, r, c)) continue
            
            // Get adjacent cells
            val adjacentCells = getAdjacentCells(currentBoard, r, c)
            
            // Remove dots from exploding cell
            currentBoard = currentBoard.updateCell(r, c, CellState.empty())
            
            // Add one dot to each adjacent cell
            val affectedCells = mutableListOf<Pair<Int, Int>>()
            for ((adjRow, adjCol) in adjacentCells) {
                val adjCell = currentBoard.getCell(adjRow, adjCol) ?: continue
                val newDots = adjCell.dots + 1
                val newCell = CellState(dots = newDots, playerId = pid)
                currentBoard = currentBoard.updateCell(adjRow, adjCol, newCell)
                affectedCells.add(Pair(adjRow, adjCol))
                
                // Check if this cell now needs to explode
                if (shouldExplode(currentBoard, adjRow, adjCol)) {
                    toProcess.add(Triple(adjRow, adjCol, pid))
                }
            }
            
            explosions.add(Explosion(r, c, pid, affectedCells))
        }
        
        return Pair(currentBoard, explosions)
    }
    
    /**
     * Gets all adjacent cells (up, down, left, right)
     */
    private fun getAdjacentCells(board: BoardState, row: Int, col: Int): List<Pair<Int, Int>> {
        val adjacent = mutableListOf<Pair<Int, Int>>()
        
        // Up
        if (row > 0) adjacent.add(Pair(row - 1, col))
        // Down
        if (row < board.gridSize - 1) adjacent.add(Pair(row + 1, col))
        // Left
        if (col > 0) adjacent.add(Pair(row, col - 1))
        // Right
        if (col < board.gridSize - 1) adjacent.add(Pair(row, col + 1))
        
        return adjacent
    }
    
    /**
     * Checks if a player has won the game
     * A player wins when all occupied cells belong to them and there are at least 2 cells
     */
    fun checkWinCondition(board: BoardState, players: List<Player>): Player? {
        val occupiedCells = board.getOccupiedCells()
        if (occupiedCells < 2) return null  // Game hasn't started properly yet
        
        for (player in players) {
            val playerCells = board.getCellCount(player.id)
            if (playerCells == occupiedCells && playerCells > 0) {
                return player
            }
        }
        
        return null
    }
    
    /**
     * Gets all valid moves for a player
     */
    fun getValidMoves(board: BoardState, player: Player): List<Move> {
        val moves = mutableListOf<Move>()
        
        for (row in 0 until board.gridSize) {
            for (col in 0 until board.gridSize) {
                if (isValidMove(board, row, col, player)) {
                    moves.add(Move(row, col, player.id))
                }
            }
        }
        
        return moves
    }
}
