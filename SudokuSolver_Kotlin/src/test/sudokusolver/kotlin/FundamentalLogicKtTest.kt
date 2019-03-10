package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

internal class FundamentalLogicKtTest {
    /*
     * 0 0 0 | 1 0 5 | 0 0 0
     * 1 4 0 | 0 0 0 | 6 7 0
     * 0 8 0 | 0 0 2 | 4 0 0
     * ------+-------+------
     * 0 6 3 | 0 7 0 | 0 1 0
     * 9 0 0 | 0 0 0 | 0 0 3
     * 0 1 0 | 0 9 0 | 5 2 0
     * ------+-------+------
     * 0 0 7 | 2 0 0 | 0 8 0
     * 0 2 6 | 0 0 0 | 0 3 5
     * 0 0 0 | 4 0 9 | 0 0 0
     */
    @Test
    fun testPruneCandidates() {
        val board = "000105000140000670080002400063070010900000003010090520007200080026000035000409000"
        val knownSolution = "672145398145983672389762451263574819958621743714398526597236184426817935831459267"
        val finalBoard = buildCellBoard(board.toOptionalBoard(), knownSolution.toBoard())
        pruneCandidates(finalBoard)

        finalBoard.getRow(0).apply {
            assertEquals(numbers(2, 3, 6, 7), this[0].candidates)
            assertEquals(numbers(3, 7, 9), this[1].candidates)
            assertEquals(numbers(2, 9), this[2].candidates)
            assertTrue(this[3].candidates.isEmpty())
            assertEquals(numbers(3, 4, 6, 8), this[4].candidates)
            assertTrue(this[5].candidates.isEmpty())
            assertEquals(numbers(2, 3, 8, 9), this[6].candidates)
            assertEquals(numbers(9), this[7].candidates)
            assertEquals(numbers(2, 8, 9), this[8].candidates)
        }
        finalBoard.getRow(1).apply {
            assertTrue(this[0].candidates.isEmpty())
            assertTrue(this[1].candidates.isEmpty())
            assertEquals(numbers(2, 5, 9), this[2].candidates)
            assertEquals(numbers(3, 8, 9), this[3].candidates)
            assertEquals(numbers(3, 8), this[4].candidates)
            assertEquals(numbers(3, 8), this[5].candidates)
            assertTrue(this[6].candidates.isEmpty())
            assertTrue(this[7].candidates.isEmpty())
            assertEquals(numbers(2, 8, 9), this[8].candidates)
        }
        finalBoard.getRow(2).apply {
            assertEquals(numbers(3, 5, 6, 7), this[0].candidates)
            assertTrue(this[1].candidates.isEmpty())
            assertEquals(numbers(5, 9), this[2].candidates)
            assertEquals(numbers(3, 6, 7, 9), this[3].candidates)
            assertEquals(numbers(3, 6), this[4].candidates)
            assertTrue(this[5].candidates.isEmpty())
            assertTrue(this[6].candidates.isEmpty())
            assertEquals(numbers(5, 9), this[7].candidates)
            assertEquals(numbers(1, 9), this[8].candidates)
        }
        finalBoard.getRow(3).apply {
            assertEquals(numbers(2, 4, 5, 8), this[0].candidates)
            assertTrue(this[1].candidates.isEmpty())
            assertTrue(this[2].candidates.isEmpty())
            assertEquals(numbers(5, 8), this[3].candidates)
            assertTrue(this[4].candidates.isEmpty())
            assertEquals(numbers(4, 8), this[5].candidates)
            assertEquals(numbers(8, 9), this[6].candidates)
            assertTrue(this[7].candidates.isEmpty())
            assertEquals(numbers(4, 8, 9), this[8].candidates)
        }
        finalBoard.getRow(4).apply {
            assertTrue(this[0].candidates.isEmpty())
            assertEquals(numbers(5, 7), this[1].candidates)
            assertEquals(numbers(2, 4, 5, 8), this[2].candidates)
            assertEquals(numbers(5, 6, 8), this[3].candidates)
            assertEquals(numbers(1, 2, 4, 5, 6, 8), this[4].candidates)
            assertEquals(numbers(1, 4, 6, 8), this[5].candidates)
            assertEquals(numbers(7, 8), this[6].candidates)
            assertEquals(numbers(4, 6), this[7].candidates)
            assertTrue(this[8].candidates.isEmpty())
        }
        finalBoard.getRow(5).apply {
            assertEquals(numbers(4, 7, 8), this[0].candidates)
            assertTrue(this[1].candidates.isEmpty())
            assertEquals(numbers(4, 8), this[2].candidates)
            assertEquals(numbers(3, 6, 8), this[3].candidates)
            assertTrue(this[4].candidates.isEmpty())
            assertEquals(numbers(3, 4, 6, 8), this[5].candidates)
            assertTrue(this[6].candidates.isEmpty())
            assertTrue(this[7].candidates.isEmpty())
            assertEquals(numbers(4, 6, 7, 8), this[8].candidates)
        }
        finalBoard.getRow(6).apply {
            assertEquals(numbers(3, 4, 5), this[0].candidates)
            assertEquals(numbers(3, 5, 9), this[1].candidates)
            assertTrue(this[2].candidates.isEmpty())
            assertTrue(this[3].candidates.isEmpty())
            assertEquals(numbers(1, 3, 5, 6), this[4].candidates)
            assertEquals(numbers(1, 3, 6), this[5].candidates)
            assertEquals(numbers(1, 9), this[6].candidates)
            assertTrue(this[7].candidates.isEmpty())
            assertEquals(numbers(1, 4, 6, 9), this[8].candidates)
        }
        finalBoard.getRow(7).apply {
            assertEquals(numbers(4, 8), this[0].candidates)
            assertTrue(this[1].candidates.isEmpty())
            assertTrue(this[2].candidates.isEmpty())
            assertEquals(numbers(7, 8), this[3].candidates)
            assertEquals(numbers(1, 8), this[4].candidates)
            assertEquals(numbers(1, 7, 8), this[5].candidates)
            assertEquals(numbers(1, 7, 9), this[6].candidates)
            assertTrue(this[7].candidates.isEmpty())
            assertTrue(this[8].candidates.isEmpty())
        }
        finalBoard.getRow(8).apply {
            assertEquals(numbers(3, 5, 8), this[0].candidates)
            assertEquals(numbers(3, 5), this[1].candidates)
            assertEquals(numbers(1, 5, 8), this[2].candidates)
            assertTrue(this[3].candidates.isEmpty())
            assertEquals(numbers(1, 3, 5, 6, 8), this[4].candidates)
            assertTrue(this[5].candidates.isEmpty())
            assertEquals(numbers(1, 2, 7), this[6].candidates)
            assertEquals(numbers(6), this[7].candidates)
            assertEquals(numbers(1, 2, 6, 7), this[8].candidates)
        }
    }

    private fun numbers(vararg numbers: Int): EnumSet<SudokuNumber> {
        val set = EnumSet.noneOf(SudokuNumber::class.java)
        set.addAll(numbers.asSequence().map { SudokuNumber.values()[it - 1] })
        return set
    }

    /*
     * 0 0 0 | 1 0 5 | 0 0 0
     * 1 4 0 | 0 0 0 | 6 7 0
     * 0 8 0 | 0 0 2 | 4 0 0
     * ------+-------+------
     * 0 6 3 | 0 7 0 | 0 1 0
     * 9 0 0 | 0 0 0 | 0 0 3
     * 0 1 0 | 0 9 0 | 5 2 0
     * ------+-------+------
     * 0 0 7 | 2 0 0 | 0 8 0
     * 0 2 6 | 0 0 0 | 0 3 5
     * 0 0 0 | 4 0 9 | 0 0 0
     */
    @Test
    fun testFillSolvedCells() {
        val board = "000105000140000670080002400063070010900000003010090520007200080026000035000409000"
        val knownSolution = "672145398145983672389762451263574819958621743714398526597236184426817935831459267"
        val finalBoard = buildCellBoard(board.toOptionalBoard(), knownSolution.toBoard())

        finalBoard.getRow(0).apply {
            this[0].setCandidates(2, 3, 6, 7)
            this[1].setCandidates(3, 7, 9)
            this[2].setCandidates(2, 9)
            this[4].setCandidates(3, 4, 6, 8)
            this[6].setCandidates(2, 3, 8, 9)
            this[7].setCandidates(9)
            this[8].setCandidates(2, 8, 9)
        }
        finalBoard.getRow(1).apply {
            this[2].setCandidates(2, 5, 9)
            this[3].setCandidates(3, 8, 9)
            this[4].setCandidates(3, 8)
            this[5].setCandidates(3, 8)
            this[8].setCandidates(2, 8, 9)
        }
        finalBoard.getRow(2).apply {
            this[0].setCandidates(3, 5, 6, 7)
            this[2].setCandidates(5, 9)
            this[3].setCandidates(3, 6, 7, 9)
            this[4].setCandidates(3, 6)
            this[7].setCandidates(5, 9)
            this[8].setCandidates(1, 9)
        }
        finalBoard.getRow(3).apply {
            this[0].setCandidates(2, 4, 5, 8)
            this[3].setCandidates(5, 8)
            this[5].setCandidates(4, 8)
            this[6].setCandidates(8, 9)
            this[8].setCandidates(4, 8, 9)
        }
        finalBoard.getRow(4).apply {
            this[1].setCandidates(5, 7)
            this[2].setCandidates(2, 4, 5, 8)
            this[3].setCandidates(5, 6, 8)
            this[4].setCandidates(1, 2, 4, 5, 6, 8)
            this[5].setCandidates(1, 4, 6, 8)
            this[6].setCandidates(7, 8)
            this[7].setCandidates(4, 6)
        }
        finalBoard.getRow(5).apply {
            this[0].setCandidates(4, 7, 8)
            this[2].setCandidates(4, 8)
            this[3].setCandidates(3, 6, 8)
            this[5].setCandidates(3, 4, 6, 8)
            this[8].setCandidates(4, 6, 7, 8)
        }
        finalBoard.getRow(6).apply {
            this[0].setCandidates(3, 4, 5)
            this[1].setCandidates(3, 5, 9)
            this[4].setCandidates(1, 3, 5, 6)
            this[5].setCandidates(1, 3, 6)
            this[6].setCandidates(1, 9)
            this[8].setCandidates(1, 4, 6, 9)
        }
        finalBoard.getRow(7).apply {
            this[0].setCandidates(4, 8)
            this[3].setCandidates(7, 8)
            this[4].setCandidates(1, 8)
            this[5].setCandidates(1, 7, 8)
            this[6].setCandidates(1, 7, 9)
        }
        finalBoard.getRow(8).apply {
            this[0].setCandidates(3, 5, 8)
            this[1].setCandidates(3, 5)
            this[2].setCandidates(1, 5, 8)
            this[4].setCandidates(1, 3, 5, 6, 8)
            this[6].setCandidates(1, 2, 7)
            this[7].setCandidates(6)
            this[8].setCandidates(1, 2, 6, 7)
        }

        fillSolvedCells(finalBoard)

        val expected = "000105090140000670080002400063070010900000003010090520007200080026000035000409060"
        assertEquals(expected.toOptionalBoard(), finalBoard.mapCells(Cell::value))
    }

    private fun Cell.setCandidates(vararg candidates: Int) {
        removeCandidates(EnumSet.complementOf(numbers(*candidates)))
    }
}