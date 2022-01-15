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
}