package com.blueedge.chainreaction.data.model

/**
 * Represents the complete state of the game board
 */
data class BoardState(
    val gridSize: Int,
    val cells: List<List<CellState>>
) {
    companion object {
        fun empty(gridSize: Int = 6): BoardState {
            val cells = List(gridSize) { List(gridSize) { CellState.empty() } }
            return BoardState(gridSize, cells)
        }
    }
    
    fun getCell(row: Int, col: Int): CellState? {
        return if (row in 0 until gridSize && col in 0 until gridSize) {
            cells[row][col]
        } else null
    }
    
    fun updateCell(row: Int, col: Int, newState: CellState): BoardState {
        val newCells = cells.mapIndexed { r, rowList ->
            if (r == row) {
                rowList.mapIndexed { c, cell ->
                    if (c == col) newState else cell
                }
            } else rowList
        }
        return copy(cells = newCells)
    }
    
    fun getCellCount(playerId: Int): Int {
        return cells.flatten().count { it.playerId == playerId }
    }
    
    fun getTotalDots(playerId: Int): Int {
        return cells.flatten()
            .filter { it.playerId == playerId }
            .sumOf { it.dots }
    }
    
    fun getOccupiedCells(): Int {
        return cells.flatten().count { !it.isEmpty() }
    }
    
    fun hasCell(row: Int, col: Int): Boolean {
        return row in 0 until gridSize && col in 0 until gridSize
    }
}
