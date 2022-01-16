package sudokusolver.kotlin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.EnumSet
import kotlin.test.assertEquals

internal class BoardModificationKtTest {
    @Test
    fun testBoardModificationRowTooLow() {
        assertEquals(
            "row is -1, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { RemoveCandidates(-1, 0, 1) }.message
        )
    }

    @Test
    fun testBoardModificationRowTooHigh() {
        assertEquals(
            "row is 9, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { RemoveCandidates(9, 0, 1) }.message
        )
    }

    @Test
    fun testBoardModificationColumnTooLow() {
        assertEquals(
            "column is -1, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { SetValue(0, -1, 1) }.message
        )
    }

    @Test
    fun testBoardModificationColumnTooHigh() {
        assertEquals(
            "column is 9, must be between 0 and 8.",
            assertThrows<IllegalArgumentException> { SetValue(0, 9, 1) }.message
        )
    }

    @Test
    fun testRemoveCandidatesCandidatesAreEmpty() {
        assertEquals(
            "candidates must not be empty.",
            assertThrows<IllegalArgumentException> { RemoveCandidates(0, 0) }.message
        )
    }

    @Test
    fun testRemoveCandidatesNotACandidateForCell() {
        assertEquals(
            "1 is not a candidate for [0, 0].",
            assertThrows<IllegalArgumentException> {
                RemoveCandidates(
                    UnsolvedCell(0, 0, EnumSet.of(SudokuNumber.TWO)),
                    EnumSet.of(SudokuNumber.ONE)
                )
            }.message
        )
    }

    @Test
    fun testSetValueNotACandidateForCell() {
        assertEquals(
            "1 is not a candidate for [0, 0].",
            assertThrows<IllegalArgumentException> {
                SetValue(UnsolvedCell(0, 0, EnumSet.of(SudokuNumber.TWO)), SudokuNumber.ONE)
            }.message
        )
    }
}