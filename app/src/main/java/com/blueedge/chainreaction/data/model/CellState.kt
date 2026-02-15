package com.blueedge.chainreaction.data.model

/**
 * Represents the state of a single cell in the game grid
 */
data class CellState(
    val dots: Int = 0,
    val playerId: Int? = null,  // null means empty cell
    val isExploding: Boolean = false
) {
    companion object {
        fun empty() = CellState(dots = 0, playerId = null)
        fun withDot(playerId: Int, dots: Int = 1) = CellState(dots = dots, playerId = playerId)
    }
    
    fun isEmpty() = playerId == null
    fun belongsTo(player: Player) = playerId == player.id
    fun canAddDot(player: Player) = isEmpty() || belongsTo(player)
}
