package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class BoardTest {
    @Test
    fun testInit() {
        val firstSize = assertThrows(IllegalArgumentException::class.java) { Board<SudokuNumber>(emptyList()) }
        assertEquals("list size is 0, must be 9.", firstSize.message)

        val secondSize = assertThrows(IllegalArgumentException::class.java) {
            Board(List(9) { emptyList<SudokuNumber>() })
        }
        assertEquals("list[0] size is 0, must be 9.", secondSize.message)

        Board(List(9) { SudokuNumber.values().toList() })
    }

    @Test
    fun get() {
        val board = Board(List(9) { SudokuNumber.values().toList() })
        assertEquals(SudokuNumber.ONE, board[0, 0])
    }

    @Test
    fun getRow() {
        val board = Board(List(9) { SudokuNumber.values().toList() })
        assertEquals(SudokuNumber.values().toList(), board.getRow(0))
    }

    @Test
    fun getColumn() {
        val board = Board(List(9) { SudokuNumber.values().toList() })
        assertEquals(List(9) { SudokuNumber.ONE }, board.getColumn(0))
    }

    @Test
    fun getBlock() {
        val board = Board(List(9) { SudokuNumber.values().toList() })
        val expected = listOf(
            SudokuNumber.ONE,
            SudokuNumber.TWO,
            SudokuNumber.THREE,
            SudokuNumber.ONE,
            SudokuNumber.TWO,
            SudokuNumber.THREE,
            SudokuNumber.ONE,
            SudokuNumber.TWO,
            SudokuNumber.THREE
        )
        assertEquals(expected, board.getBlock(BlockIndex(0, 0)))
    }

    @Test
    fun equals() {
        val board1 = Board(List(9) { SudokuNumber.values().toList() })
        val board2 = Board(List(9) { SudokuNumber.values().toList() })
        assertEquals(board1, board2)
    }

    @Test
    fun testHashCode() {
        val board = Board(List(9) { SudokuNumber.values().toList() })
        board.hashCode()
    }

    @Test
    fun testToString() {
        val board = Board(List(9) { SudokuNumber.values().toList() })
        val expected = """
            1 2 3 | 4 5 6 | 7 8 9
            1 2 3 | 4 5 6 | 7 8 9
            1 2 3 | 4 5 6 | 7 8 9
            ------+-------+------
            1 2 3 | 4 5 6 | 7 8 9
            1 2 3 | 4 5 6 | 7 8 9
            1 2 3 | 4 5 6 | 7 8 9
            ------+-------+------
            1 2 3 | 4 5 6 | 7 8 9
            1 2 3 | 4 5 6 | 7 8 9
            1 2 3 | 4 5 6 | 7 8 9
        """.trimIndent()
        assertEquals(expected, board.toString())
    }

    @Test
    fun toMutableBoard() {
        val board = Board(List(9) { SudokuNumber.values().toList() })
        assertEquals(board, board.toMutableBoard())
    }
}