package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SudokuNumberTest {
    @Test
    fun testToString() {
        assertEquals("1", SudokuNumber.ONE.toString())
        assertEquals("2", SudokuNumber.TWO.toString())
        assertEquals("3", SudokuNumber.THREE.toString())
        assertEquals("4", SudokuNumber.FOUR.toString())
        assertEquals("5", SudokuNumber.FIVE.toString())
        assertEquals("6", SudokuNumber.SIX.toString())
        assertEquals("7", SudokuNumber.SEVEN.toString())
        assertEquals("8", SudokuNumber.EIGHT.toString())
        assertEquals("9", SudokuNumber.NINE.toString())
    }
}