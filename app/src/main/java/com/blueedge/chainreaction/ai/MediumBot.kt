package com.blueedge.chainreaction.ai

import com.blueedge.chainreaction.data.model.BoardState
import com.blueedge.chainreaction.data.model.CellState
import com.blueedge.chainreaction.data.model.Move
import com.blueedge.chainreaction.data.model.Player
import com.blueedge.chainreaction.domain.GameEngine
import javax.inject.Inject

/**
 * Medium bot that uses defensive strategy
 * Prioritizes:
 * 1. Blocking opponent's potential explosions
 * 2. Maximizing own territory
 * 3. Building up cells near opponent
 */
class MediumBot @Inject constructor(
    private val gameEngine: GameEngine
) : BotStrategy {
    
    override suspend fun calculateMove(
        boardState: BoardState,
        botPlayer: Player,
        opponentPlayer: Player
    ): Move? {
        val validMoves = gameEngine.getValidMoves(boardState, botPlayer)
        if (validMoves.isEmpty()) return null
        
        // Score each move
        val scoredMoves = validMoves.map { move ->
            val score = evaluateMove(boardState, move, botPlayer, opponentPlayer)
            move to score
        }
        
        // Return the move with highest score
        return scoredMoves.maxByOrNull { it.second }?.first
    }
    
    private fun evaluateMove(
        boardState: BoardState,
        move: Move,
        botPlayer: Player,
        opponentPlayer: Player
    ): Int {
        var score = 0
        
        // Simulate the move
        val result = gameEngine.addDot(boardState, move.row, move.col, botPlayer)
        val newBoard = result.newBoard
        
        // Factor 1: Territory control (30 points per cell gained)
        val territoriesGained = newBoard.getCellCount(botPlayer.id) - boardState.getCellCount(botPlayer.id)
        score += territoriesGained * 30
        
        // Factor 2: Cells captured from opponent (50 points per captured cell)
        val opponentCellsBefore = boardState.getCellCount(opponentPlayer.id)
        val opponentCellsAfter = newBoard.getCellCount(opponentPlayer.id)
        val capturedCells = opponentCellsBefore - opponentCellsAfter
        score += capturedCells * 50
        
        // Factor 3: Blocking opponent's dangerous cells (cells close to explosion)
        val adjacentCells = getAdjacentCells(boardState, move.row, move.col)
        for ((adjRow, adjCol) in adjacentCells) {
            val adjCell = boardState.getCell(adjRow, adjCol)
            if (adjCell?.playerId == opponentPlayer.id) {
                val criticalMass = getCriticalMass(boardState, adjRow, adjCol)
                val dotsNeeded = criticalMass - adjCell.dots
                if (dotsNeeded <= 1) {
                    // Opponent cell is close to exploding, block it
                    score += 40
                }
            }
        }
        
        // Factor 4: Building up own cells (10 points for each dot added to existing cell)
        val cell = boardState.getCell(move.row, move.col)
        if (cell?.playerId == botPlayer.id) {
            score += cell.dots * 10
        }
        
        // Factor 5: Prefer corner and edge cells early game (5 points)
        val isCornerOrEdge = isCornerOrEdge(boardState, move.row, move.col)
        if (isCornerOrEdge && boardState.getOccupiedCells() < 10) {
            score += 5
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
    
    private fun getAdjacentCells(board: BoardState, row: Int, col: Int): List<Pair<Int, Int>> {
        val adjacent = mutableListOf<Pair<Int, Int>>()
        if (row > 0) adjacent.add(Pair(row - 1, col))
        if (row < board.gridSize - 1) adjacent.add(Pair(row + 1, col))
        if (col > 0) adjacent.add(Pair(row, col - 1))
        if (col < board.gridSize - 1) adjacent.add(Pair(row, col + 1))
        return adjacent
    }
    
    private fun isCornerOrEdge(board: BoardState, row: Int, col: Int): Boolean {
        return row == 0 || row == board.gridSize - 1 || 
               col == 0 || col == board.gridSize - 1
    }
}
