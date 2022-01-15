package sudokusolver.kotlin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.EnumSet
import kotlin.test.assertEquals

internal class CellKtTest {
    @Test
    fun testCellRowTooLow() {
        assertEquals(
            "row is -1, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { SolvedCell(-1, 0, SudokuNumber.ONE) }.message
        )
    }

    @Test
    fun testCellRowTooHigh() {
        assertEquals(
            "row is 9, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { SolvedCell(9, 0, SudokuNumber.ONE) }.message
        )
    }

    @Test
    fun testCellColumnTooLow() {
        assertEquals(
            "column is -1, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { UnsolvedCell(0, -1) }.message
        )
    }

    @Test
    fun testCellColumnTooHigh() {
        assertEquals(
            "column is 9, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { UnsolvedCell(0, 9) }.message
        )
    }

    @Test
    fun testUnsolvedCellCandidatesAreEmpty() {
        assertEquals(
            "candidates must not be empty.",
            assertThrows<IllegalArgumentException> {
                UnsolvedCell(0, 0, EnumSet.noneOf(SudokuNumber::class.java))
            }.message
        )
    }

    @Test
    fun testParseSimpleCellsWrongLength() {
        assertEquals(
            "simpleBoard.length is 0, must be 81.",
            assertThrows<IllegalArgumentException> { parseSimpleCells("") }.message
        )
    }

    @Test
    fun testParseCellsWithCandidatesUnmatchedOpeningBrace() {
        assertEquals(
            "Unmatched '{'.",
            assertThrows<IllegalArgumentException> { parseCellsWithCandidates("{") }.message
        )
    }

    @Test
    fun testParseCellsWithCandidatesEmptyBraces() {
        assertEquals(
            "Empty \"{}\".",
            assertThrows<IllegalArgumentException> { parseCellsWithCandidates("{}") }.message
        )
    }

    @Test
    fun testParseCellsWithCandidatesNestedBrace() {
        assertEquals(
            "Nested '{'.",
            assertThrows<IllegalArgumentException> { parseCellsWithCandidates("{{}") }.message
        )
    }

    @Test
    fun testParseCellsWithCandidatesInvalidCharacterInBraces() {
        assertEquals(
            "Invalid character: 'a'.",
            assertThrows<IllegalArgumentException> { parseCellsWithCandidates("{a}") }.message
        )
    }

    @Test
    fun testParseCellsWithCandidatesUnmatchedClosingBrace() {
        assertEquals(
            "Unmatched '}'.",
            assertThrows<IllegalArgumentException> { parseCellsWithCandidates("}") }.message
        )
    }

    @Test
    fun testParseCellsWithCandidatesInvalidCharacter() {
        assertEquals(
            "Invalid character: 'a'.",
            assertThrows<IllegalArgumentException> { parseCellsWithCandidates("a") }.message
        )
    }

    @Test
    fun testParseCellsWithCandidatesWrongLength() {
        assertEquals(
            "Found 0 cells, required 81.",
            assertThrows<IllegalArgumentException> { parseCellsWithCandidates("") }.message
        )
    }
}