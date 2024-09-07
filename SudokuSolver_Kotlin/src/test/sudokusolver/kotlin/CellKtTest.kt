package sudokusolver.kotlin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.EnumSet
import kotlin.test.assertEquals

internal class CellKtTest {
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