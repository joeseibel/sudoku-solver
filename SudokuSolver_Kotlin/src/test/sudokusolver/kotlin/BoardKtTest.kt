package sudokusolver.kotlin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class BoardKtTest {
    @Test
    fun testAbstractBoardGetBlockIndexTooLow() {
        val board = Board(List(UNIT_SIZE) { row -> List(UNIT_SIZE) { column -> row * UNIT_SIZE + column } })
        assertEquals(
            "blockIndex is -1, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { board.getBlock(-1) }.message
        )
    }

    @Test
    fun testAbstractBoardGetBlockIndexTooHigh() {
        val board = Board(List(UNIT_SIZE) { row -> List(UNIT_SIZE) { column -> row * UNIT_SIZE + column } })
        assertEquals(
            "blockIndex is 9, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { board.getBlock(9) }.message
        )
    }

    @Test
    fun testAbstractBoardWrongSize() {
        assertEquals(
            "elements size is 0, must be 9.",
            assertThrows<IllegalArgumentException> { Board(emptyList<List<Any>>()) }.message
        )
    }

    @Test
    fun testAbstractBoardWrongInnerSize() {
        assertEquals(
            "elements[0] size is 0, must be 9.",
            assertThrows<IllegalArgumentException> { Board(List(UNIT_SIZE) { emptyList<Any>() }) }.message
        )
    }

    @Test
    fun testRowTooLow() {
        assertEquals(
            "row is -1, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { RemoveCandidates(-1, 0, 1) }.message
        )
    }

    @Test
    fun testRowTooHigh() {
        assertEquals(
            "row is 9, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { SetValue(9, 0, 1) }.message
        )
    }

    @Test
    fun testColumnTooLow() {
        assertEquals(
            "column is -1, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { SolvedCell(0, -1, SudokuNumber.ONE) }.message
        )
    }

    @Test
    fun testColumnTooHigh() {
        assertEquals(
            "column is 9, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { UnsolvedCell(0, 9) }.message
        )
    }
}