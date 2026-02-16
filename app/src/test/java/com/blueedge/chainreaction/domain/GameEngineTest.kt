package com.blueedge.chainreaction.domain

import com.blueedge.chainreaction.data.CellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setup() {
        engine = GameEngine()
    }

    @Test
    fun `createEmptyBoard creates correct size`() {
        val board = engine.createEmptyBoard(6)
        assertEquals(6, board.size)
        assertEquals(6, board[0].size)
        board.forEach { row ->
            row.forEach { cell ->
                assertTrue(cell.isEmpty)
                assertEquals(0, cell.ownerId)
                assertEquals(0, cell.dots)
            }
        }
    }

    @Test
    fun `isValidMove on empty cell returns true for any player`() {
        val board = engine.createEmptyBoard(5)
        assertTrue(engine.isValidMove(board, 0, 0, 1))
        assertTrue(engine.isValidMove(board, 0, 0, 2))
    }

    @Test
    fun `isValidMove on own cell returns true`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[2] = this[2].toMutableList().apply {
                this[2] = CellState(ownerId = 1, dots = 2)
            }
        }
        assertTrue(engine.isValidMove(board, 2, 2, 1))
    }

    @Test
    fun `isValidMove on opponent cell returns false`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[2] = this[2].toMutableList().apply {
                this[2] = CellState(ownerId = 1, dots = 2)
            }
        }
        assertFalse(engine.isValidMove(board, 2, 2, 2))
    }

    @Test
    fun `isValidMove out of bounds returns false`() {
        val board = engine.createEmptyBoard(5)
        assertFalse(engine.isValidMove(board, -1, 0, 1))
        assertFalse(engine.isValidMove(board, 0, -1, 1))
        assertFalse(engine.isValidMove(board, 5, 0, 1))
        assertFalse(engine.isValidMove(board, 0, 5, 1))
    }

    @Test
    fun `executeMove adds dot to empty cell`() {
        val board = engine.createEmptyBoard(5)
        val (newBoard, explosions, _) = engine.executeMove(board, 2, 2, 1)

        assertEquals(1, newBoard[2][2].ownerId)
        assertEquals(1, newBoard[2][2].dots)
        assertTrue(explosions.isEmpty())
    }

    @Test
    fun `executeMove adds dot to own cell`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[2] = this[2].toMutableList().apply {
                this[2] = CellState(ownerId = 1, dots = 2)
            }
        }
        val (newBoard, explosions, _) = engine.executeMove(board, 2, 2, 1)

        assertEquals(1, newBoard[2][2].ownerId)
        assertEquals(3, newBoard[2][2].dots)
        assertTrue(explosions.isEmpty())
    }

    @Test
    fun `explosion at center cell spreads to 4 neighbors`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[2] = this[2].toMutableList().apply {
                this[2] = CellState(ownerId = 1, dots = 3)
            }
        }
        val (newBoard, explosions, _) = engine.executeMove(board, 2, 2, 1)

        // Center should be empty after explosion
        assertTrue(newBoard[2][2].isEmpty)

        // All 4 neighbors should have 1 dot each, owned by player 1
        assertEquals(1, newBoard[1][2].dots)
        assertEquals(1, newBoard[1][2].ownerId)
        assertEquals(1, newBoard[3][2].dots)
        assertEquals(1, newBoard[3][2].ownerId)
        assertEquals(1, newBoard[2][1].dots)
        assertEquals(1, newBoard[2][1].ownerId)
        assertEquals(1, newBoard[2][3].dots)
        assertEquals(1, newBoard[2][3].ownerId)

        // Should have 1 explosion wave
        assertEquals(1, explosions.size)
    }

    @Test
    fun `explosion at corner cell spreads to 2 neighbors`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[0] = this[0].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 3)
            }
        }
        val (newBoard, explosions, _) = engine.executeMove(board, 0, 0, 1)

        // Corner should be empty
        assertTrue(newBoard[0][0].isEmpty)

        // Only 2 neighbors exist for corner
        assertEquals(1, newBoard[0][1].dots)
        assertEquals(1, newBoard[0][1].ownerId)
        assertEquals(1, newBoard[1][0].dots)
        assertEquals(1, newBoard[1][0].ownerId)

        assertEquals(1, explosions.size)
    }

    @Test
    fun `explosion at edge cell spreads to 3 neighbors`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[0] = this[0].toMutableList().apply {
                this[2] = CellState(ownerId = 1, dots = 3)
            }
        }
        val (newBoard, explosions, _) = engine.executeMove(board, 0, 2, 1)

        // Edge cell should be empty
        assertTrue(newBoard[0][2].isEmpty)

        // 3 neighbors for edge
        assertEquals(1, newBoard[0][1].dots)
        assertEquals(1, newBoard[0][3].dots)
        assertEquals(1, newBoard[1][2].dots)

        assertEquals(1, explosions.size)
    }

    @Test
    fun `explosion captures opponent cells`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[2] = this[2].toMutableList().apply {
                this[2] = CellState(ownerId = 1, dots = 3) // Will explode
                this[3] = CellState(ownerId = 2, dots = 2) // Opponent cell
            }
        }
        val (newBoard, _, _) = engine.executeMove(board, 2, 2, 1)

        // The opponent cell at (2,3) should now belong to player 1
        assertEquals(1, newBoard[2][3].ownerId)
        assertEquals(3, newBoard[2][3].dots) // Was 2, +1 from explosion = 3
    }

    @Test
    fun `chain reaction works correctly`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[2] = this[2].toMutableList().apply {
                this[2] = CellState(ownerId = 1, dots = 3) // Will explode
            }
            this[2] = this[2].toMutableList().apply {
                this[3] = CellState(ownerId = 2, dots = 3) // Will chain from explosion
            }
        }
        val (newBoard, explosions, _) = engine.executeMove(board, 2, 2, 1)

        // Should have at least 2 explosion waves (original + chain)
        assertTrue(explosions.size >= 2)

        // The chain should make (2,3) explode too,
        // spreading player 1's color further
        // (2,3) should be empty after its explosion
        assertTrue(newBoard[2][3].isEmpty)
    }

    @Test
    fun `checkWinCondition returns null before 2 moves`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[0] = this[0].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 1)
            }
        }
        assertNull(engine.checkWinCondition(board, 1))
    }

    @Test
    fun `checkWinCondition returns winner when all cells belong to one player`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[0] = this[0].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 1)
            }
            this[1] = this[1].toMutableList().apply {
                this[1] = CellState(ownerId = 1, dots = 2)
            }
        }
        val winner = engine.checkWinCondition(board, 3)
        assertNotNull(winner)
        assertEquals(1, winner)
    }

    @Test
    fun `checkWinCondition returns null when both players have cells`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[0] = this[0].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 1)
            }
            this[1] = this[1].toMutableList().apply {
                this[1] = CellState(ownerId = 2, dots = 1)
            }
        }
        assertNull(engine.checkWinCondition(board, 3))
    }

    @Test
    fun `getValidMoves returns all valid cells for player`() {
        val board = engine.createEmptyBoard(3).toMutableList().apply {
            this[0] = this[0].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 1)
            }
            this[1] = this[1].toMutableList().apply {
                this[1] = CellState(ownerId = 2, dots = 1)
            }
        }
        val moves = engine.getValidMoves(board, 1)
        // Player 1 can click: own cell (0,0) + all empty cells (8-1=7 empty)
        // Cannot click player 2's cell (1,1)
        assertEquals(8, moves.size) // 1 owned + 7 empty = 8
        assertFalse(moves.any { it.row == 1 && it.col == 1 })
    }

    @Test
    fun `countPlayerCells counts correctly`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[0] = this[0].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 1)
            }
            this[1] = this[1].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 2)
                this[1] = CellState(ownerId = 2, dots = 1)
            }
        }
        assertEquals(2, engine.countPlayerCells(board, 1))
        assertEquals(1, engine.countPlayerCells(board, 2))
    }

    @Test
    fun `countPlayerDots counts correctly`() {
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[0] = this[0].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 1)
            }
            this[1] = this[1].toMutableList().apply {
                this[0] = CellState(ownerId = 1, dots = 3)
                this[1] = CellState(ownerId = 2, dots = 2)
            }
        }
        assertEquals(4, engine.countPlayerDots(board, 1)) // 1 + 3
        assertEquals(2, engine.countPlayerDots(board, 2))
    }

    @Test
    fun `simultaneous explosions work correctly`() {
        // Two adjacent cells both at 3 dots
        val board = engine.createEmptyBoard(5).toMutableList().apply {
            this[2] = this[2].toMutableList().apply {
                this[1] = CellState(ownerId = 1, dots = 3)
                this[2] = CellState(ownerId = 1, dots = 3)
            }
        }

        // Adding a dot to (2,1) makes it 4 -> explodes -> (2,2) gets +1 -> becomes 4 -> also explodes
        val (newBoard, explosions, _) = engine.executeMove(board, 2, 1, 1)

        // Should have multiple waves
        assertTrue(explosions.isNotEmpty())

        // Both original cells should be processed
        // After all chain reactions, the board should be valid
        var totalDots = 0
        var allPlayer1 = true
        for (row in newBoard) {
            for (cell in row) {
                if (!cell.isEmpty) {
                    totalDots += cell.dots
                    if (cell.ownerId != 1) allPlayer1 = false
                }
            }
        }
        // All occupied cells should belong to player 1
        assertTrue(allPlayer1)
        // Total dots should be preserved (original 7 dots - lost to edges)
        assertTrue(totalDots > 0)
    }

    @Test
    fun `all grid sizes create correct boards`() {
        val sizes = listOf(5, 6, 7, 8, 10)
        for (size in sizes) {
            val board = engine.createEmptyBoard(size)
            assertEquals(size, board.size)
            assertEquals(size, board[0].size)
        }
    }
}
