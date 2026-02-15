package com.blueedge.chainreaction.data.model

/**
 * Represents a move in the game
 */
data class Move(
    val row: Int,
    val col: Int,
    val playerId: Int
)

/**
 * Represents the result of a move with potential explosions
 */
data class MoveResult(
    val newBoard: BoardState,
    val explosions: List<Explosion>
)

/**
 * Represents a single explosion event
 */
data class Explosion(
    val row: Int,
    val col: Int,
    val playerId: Int,
    val affectedCells: List<Pair<Int, Int>>  // List of (row, col) that received dots
)
