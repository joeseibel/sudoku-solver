package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MutableBoardTest {

    @Test
    fun set() {
        val board = MutableBoard(List(9) { SudokuNumber.values().toMutableList<SudokuNumber?>() })
        board[0, 0] = null
        assertNull(board[0, 0])
    }

    @Test
    fun mapCellsToBoard() {
        val board = MutableBoard(List(9) { SudokuNumber.values().toMutableList() })
        val expected = Board(List(9) { listOf(0, 1, 2, 3, 4, 5, 6, 7, 8) })
        assertEquals(expected, board.mapCellsToBoard { it.ordinal })
    }
}