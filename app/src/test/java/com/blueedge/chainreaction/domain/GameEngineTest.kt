package com.blueedge.chainreaction.domain

import androidx.compose.ui.graphics.Color
import com.blueedge.chainreaction.data.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {
    
    private lateinit var gameEngine: GameEngine
    private lateinit var player1: Player
    private lateinit var player2: Player
    
    @Before
    fun setup() {
        gameEngine = GameEngine()
        player1 = Player(1, "Player 1", Color.Blue)
        player2 = Player(2, "Player 2", Color.Red)
    }
    
    @Test
    fun `empty cell is valid for any player`() {
        val board = BoardState.empty(5)
        assertTrue(gameEngine.isValidMove(board, 0, 0, player1))
        assertTrue(gameEngine.isValidMove(board, 0, 0, player2))
    }
    
    @Test
    fun `occupied cell is only valid for owning player`() {
        val board = BoardState.empty(5)
            .updateCell(0, 0, CellState.withDot(player1.id, 1))
        
        assertTrue(gameEngine.isValidMove(board, 0, 0, player1))
        assertFalse(gameEngine.isValidMove(board, 0, 0, player2))
    }
    
    @Test
    fun `adding dot increases dot count`() {
        val board = BoardState.empty(5)
        val result = gameEngine.addDot(board, 0, 0, player1)
        
        assertEquals(1, result.newBoard.getCell(0, 0)?.dots)
        assertEquals(player1.id, result.newBoard.getCell(0, 0)?.playerId)
    }
    
    @Test
    fun `corner cell explodes at 2 dots`() {
        val board = BoardState.empty(5)
            .updateCell(0, 0, CellState.withDot(player1.id, 1))
        
        val result = gameEngine.addDot(board, 0, 0, player1)
        
        // Corner cell should be empty after explosion
        assertTrue(result.newBoard.getCell(0, 0)?.isEmpty() ?: false)
        
        // Adjacent cells should have 1 dot each
        assertEquals(1, result.newBoard.getCell(0, 1)?.dots)  // Right
        assertEquals(1, result.newBoard.getCell(1, 0)?.dots)  // Down
        
        // Both adjacent cells should belong to player1
        assertEquals(player1.id, result.newBoard.getCell(0, 1)?.playerId)
        assertEquals(player1.id, result.newBoard.getCell(1, 0)?.playerId)
        
        // Should have 1 explosion recorded
        assertEquals(1, result.explosions.size)
        assertEquals(2, result.explosions[0].affectedCells.size)
    }
    
    @Test
    fun `edge cell explodes at 3 dots`() {
        val board = BoardState.empty(5)
            .updateCell(0, 2, CellState.withDot(player1.id, 2))
        
        val result = gameEngine.addDot(board, 0, 2, player1)
        
        // Edge cell should be empty
        assertTrue(result.newBoard.getCell(0, 2)?.isEmpty() ?: false)
        
        // Three adjacent cells should have 1 dot each
        assertEquals(1, result.newBoard.getCell(0, 1)?.dots)  // Left
        assertEquals(1, result.newBoard.getCell(0, 3)?.dots)  // Right
        assertEquals(1, result.newBoard.getCell(1, 2)?.dots)  // Down
        
        assertEquals(1, result.explosions.size)
        assertEquals(3, result.explosions[0].affectedCells.size)
    }
    
    @Test
    fun `center cell explodes at 4 dots`() {
        val board = BoardState.empty(5)
            .updateCell(2, 2, CellState.withDot(player1.id, 3))
        
        val result = gameEngine.addDot(board, 2, 2, player1)
        
        // Center cell should be empty
        assertTrue(result.newBoard.getCell(2, 2)?.isEmpty() ?: false)
        
        // Four adjacent cells should have 1 dot each
        assertEquals(1, result.newBoard.getCell(1, 2)?.dots)  // Up
        assertEquals(1, result.newBoard.getCell(3, 2)?.dots)  // Down
        assertEquals(1, result.newBoard.getCell(2, 1)?.dots)  // Left
        assertEquals(1, result.newBoard.getCell(2, 3)?.dots)  // Right
        
        assertEquals(1, result.explosions.size)
        assertEquals(4, result.explosions[0].affectedCells.size)
    }
    
    @Test
    fun `explosion captures opponent cell`() {
        var board = BoardState.empty(5)
        board = board.updateCell(0, 0, CellState.withDot(player1.id, 1))
        board = board.updateCell(0, 1, CellState.withDot(player2.id, 1))
        
        val result = gameEngine.addDot(board, 0, 0, player1)
        
        // Adjacent cell should now belong to player1
        assertEquals(player1.id, result.newBoard.getCell(0, 1)?.playerId)
        assertEquals(2, result.newBoard.getCell(0, 1)?.dots)
    }
    
    @Test
    fun `chain reaction works correctly`() {
        // Set up a chain: corner cell with 1 dot, adjacent cell with 1 dot
        var board = BoardState.empty(5)
        board = board.updateCell(0, 0, CellState.withDot(player1.id, 1))
        board = board.updateCell(0, 1, CellState.withDot(player1.id, 2))  // Edge cell with 2 dots
        
        // Adding to corner should trigger chain
        val result = gameEngine.addDot(board, 0, 0, player1)
        
        // Both cells should have exploded
        assertTrue(result.newBoard.getCell(0, 0)?.isEmpty() ?: false)
        assertTrue(result.newBoard.getCell(0, 1)?.isEmpty() ?: false)
        
        // Should have multiple explosions
        assertTrue(result.explosions.size >= 2)
    }
    
    @Test
    fun `win condition not detected with less than 2 cells`() {
        val board = BoardState.empty(5)
            .updateCell(0, 0, CellState.withDot(player1.id, 1))
        
        assertNull(gameEngine.checkWinCondition(board, listOf(player1, player2)))
    }
    
    @Test
    fun `win condition detected when one player owns all cells`() {
        var board = BoardState.empty(5)
        board = board.updateCell(0, 0, CellState.withDot(player1.id, 1))
        board = board.updateCell(0, 1, CellState.withDot(player1.id, 1))
        board = board.updateCell(1, 0, CellState.withDot(player1.id, 2))
        
        val winner = gameEngine.checkWinCondition(board, listOf(player1, player2))
        assertEquals(player1, winner)
    }
    
    @Test
    fun `win condition not detected when multiple players have cells`() {
        var board = BoardState.empty(5)
        board = board.updateCell(0, 0, CellState.withDot(player1.id, 1))
        board = board.updateCell(0, 1, CellState.withDot(player2.id, 1))
        board = board.updateCell(1, 0, CellState.withDot(player1.id, 2))
        
        assertNull(gameEngine.checkWinCondition(board, listOf(player1, player2)))
    }
    
    @Test
    fun `getValidMoves returns empty cells and player's own cells`() {
        var board = BoardState.empty(5)
        board = board.updateCell(0, 0, CellState.withDot(player1.id, 1))
        board = board.updateCell(0, 1, CellState.withDot(player2.id, 1))
        
        val moves = gameEngine.getValidMoves(board, player1)
        
        // Should include all empty cells (25 - 2 = 23) + player1's cell (1) = 24
        assertEquals(24, moves.size)
        
        // Should include player1's cell
        assertTrue(moves.any { it.row == 0 && it.col == 0 })
        
        // Should not include player2's cell
        assertFalse(moves.any { it.row == 0 && it.col == 1 })
    }
    
    @Test
    fun `corner cells explode correctly at all corners`() {
        val corners = listOf(
            Pair(0, 0),  // Top-left
            Pair(0, 4),  // Top-right
            Pair(4, 0),  // Bottom-left
            Pair(4, 4)   // Bottom-right
        )
        
        for ((row, col) in corners) {
            val board = BoardState.empty(5)
                .updateCell(row, col, CellState.withDot(player1.id, 1))
            
            val result = gameEngine.addDot(board, row, col, player1)
            
            // Corner should be empty
            assertTrue("Corner ($row, $col) should be empty", 
                result.newBoard.getCell(row, col)?.isEmpty() ?: false)
            
            // Should have exactly 2 affected cells
            assertEquals("Corner ($row, $col) should affect 2 cells", 
                2, result.explosions[0].affectedCells.size)
        }
    }
    
    @Test
    fun `complex chain reaction scenario`() {
        // Create a setup where multiple cells will chain explode
        var board = BoardState.empty(5)
        // Create a line of cells ready to explode
        board = board.updateCell(2, 0, CellState.withDot(player1.id, 2))  // Edge: needs 3
        board = board.updateCell(2, 1, CellState.withDot(player1.id, 3))  // Center: needs 4
        board = board.updateCell(2, 2, CellState.withDot(player1.id, 3))  // Center: needs 4
        
        val result = gameEngine.addDot(board, 2, 0, player1)
        
        // Should have multiple explosions
        assertTrue(result.explosions.size >= 1)
        
        // Original cell should be empty
        assertTrue(result.newBoard.getCell(2, 0)?.isEmpty() ?: false)
    }
    
    @Test
    fun `board state methods work correctly`() {
        var board = BoardState.empty(5)
        board = board.updateCell(0, 0, CellState.withDot(player1.id, 1))
        board = board.updateCell(0, 1, CellState.withDot(player1.id, 2))
        board = board.updateCell(1, 0, CellState.withDot(player2.id, 1))
        
        assertEquals(2, board.getCellCount(player1.id))
        assertEquals(1, board.getCellCount(player2.id))
        assertEquals(3, board.getTotalDots(player1.id))
        assertEquals(1, board.getTotalDots(player2.id))
        assertEquals(3, board.getOccupiedCells())
    }
}
