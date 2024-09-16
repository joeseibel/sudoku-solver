package sudokusolver.kotlin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class SudokuNumberKtTest {
    @Test
    fun testSudokuNumberUnexpectedChar() {
        assertEquals(
            "ch is 'a', must be between '1' and '9'.",
            assertThrows<IllegalArgumentException> { sudokuNumber('a') }.message
        )
    }

    @Test
    fun testParseOptionalBoardWrongLength() {
        assertEquals(
            "board.length is 0, must be 81.",
            assertThrows<IllegalArgumentException> { parseOptionalBoard("") }.message
        )
    }

    @Test
    fun testParseBoardWrongLength() {
        assertEquals(
            "board.length is 0, must be 81.",
            assertThrows<IllegalArgumentException> { parseBoard("") }.message
        )
    }
}