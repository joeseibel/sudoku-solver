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
        val modifications = pruneCandidates(finalBoard)

        assertEquals(53, modifications.size)
        assertEquals(RemoveCandidates(0, 0, numbers(1, 4, 5, 8, 9)), modifications[0])
        assertEquals(RemoveCandidates(0, 1, numbers(1, 2, 4, 5, 6, 8)), modifications[1])
        assertEquals(RemoveCandidates(0, 2, numbers(1, 3, 4, 5, 6, 7, 8)), modifications[2])
        assertEquals(RemoveCandidates(0, 4, numbers(1, 2, 5, 7, 9)), modifications[3])
        assertEquals(RemoveCandidates(0, 6, numbers(1, 4, 5, 6, 7)), modifications[4])
        assertEquals(RemoveCandidates(0, 7, numbers(1, 2, 3, 4, 5, 6, 7, 8)), modifications[5])
        assertEquals(RemoveCandidates(0, 8, numbers(1, 3, 4, 5, 6, 7)), modifications[6])
        assertEquals(RemoveCandidates(1, 2, numbers(1, 3, 4, 6, 7, 8)), modifications[7])
        assertEquals(RemoveCandidates(1, 3, numbers(1, 2, 4, 5, 6, 7)), modifications[8])
        assertEquals(RemoveCandidates(1, 4, numbers(1, 2, 4, 5, 6, 7, 9)), modifications[9])
        assertEquals(RemoveCandidates(1, 5, numbers(1, 2, 4, 5, 6, 7, 9)), modifications[10])
        assertEquals(RemoveCandidates(1, 8, numbers(1, 3, 4, 5, 6, 7)), modifications[11])
        assertEquals(RemoveCandidates(2, 0, numbers(1, 2, 4, 8, 9)), modifications[12])
        assertEquals(RemoveCandidates(2, 2, numbers(1, 2, 3, 4, 6, 7, 8)), modifications[13])
        assertEquals(RemoveCandidates(2, 3, numbers(1, 2, 4, 5, 8)), modifications[14])
        assertEquals(RemoveCandidates(2, 4, numbers(1, 2, 4, 5, 7, 8, 9)), modifications[15])
        assertEquals(RemoveCandidates(2, 7, numbers(1, 2, 3, 4, 6, 7, 8)), modifications[16])
        assertEquals(RemoveCandidates(2, 8, numbers(2, 3, 4, 5, 6, 7, 8)), modifications[17])
        assertEquals(RemoveCandidates(3, 0, numbers(1, 3, 6, 7, 9)), modifications[18])
        assertEquals(RemoveCandidates(3, 3, numbers(1, 2, 3, 4, 6, 7, 9)), modifications[19])
        assertEquals(RemoveCandidates(3, 5, numbers(1, 2, 3, 5, 6, 7, 9)), modifications[20])
        assertEquals(RemoveCandidates(3, 6, numbers(1, 2, 3, 4, 5, 6, 7)), modifications[21])
        assertEquals(RemoveCandidates(3, 8, numbers(1, 2, 3, 5, 6, 7)), modifications[22])
        assertEquals(RemoveCandidates(4, 1, numbers(1, 2, 3, 4, 6, 8, 9)), modifications[23])
        assertEquals(RemoveCandidates(4, 2, numbers(1, 3, 6, 7, 9)), modifications[24])
        assertEquals(RemoveCandidates(4, 3, numbers(1, 2, 3, 4, 7, 9)), modifications[25])
        assertEquals(RemoveCandidates(4, 4, numbers(3, 7, 9)), modifications[26])
        assertEquals(RemoveCandidates(4, 5, numbers(2, 3, 5, 7, 9)), modifications[27])
        assertEquals(RemoveCandidates(4, 6, numbers(1, 2, 3, 4, 5, 6, 9)), modifications[28])
        assertEquals(RemoveCandidates(4, 7, numbers(1, 2, 3, 5, 7, 8, 9)), modifications[29])
        assertEquals(RemoveCandidates(5, 0, numbers(1, 2, 3, 5, 6, 9)), modifications[30])
        assertEquals(RemoveCandidates(5, 2, numbers(1, 2, 3, 5, 6, 7, 9)), modifications[31])
        assertEquals(RemoveCandidates(5, 3, numbers(1, 2, 4, 5, 7, 9)), modifications[32])
        assertEquals(RemoveCandidates(5, 5, numbers(1, 2, 5, 7, 9)), modifications[33])
        assertEquals(RemoveCandidates(5, 8, numbers(1, 2, 3, 5, 9)), modifications[34])
        assertEquals(RemoveCandidates(6, 0, numbers(1, 2, 6, 7, 8, 9)), modifications[35])
        assertEquals(RemoveCandidates(6, 1, numbers(1, 2, 4, 6, 7, 8)), modifications[36])
        assertEquals(RemoveCandidates(6, 4, numbers(2, 4, 7, 8, 9)), modifications[37])
        assertEquals(RemoveCandidates(6, 5, numbers(2, 4, 5, 7, 8, 9)), modifications[38])
        assertEquals(RemoveCandidates(6, 6, numbers(2, 3, 4, 5, 6, 7, 8)), modifications[39])
        assertEquals(RemoveCandidates(6, 8, numbers(2, 3, 5, 7, 8)), modifications[40])
        assertEquals(RemoveCandidates(7, 0, numbers(1, 2, 3, 5, 6, 7, 9)), modifications[41])
        assertEquals(RemoveCandidates(7, 3, numbers(1, 2, 3, 4, 5, 6, 9)), modifications[42])
        assertEquals(RemoveCandidates(7, 4, numbers(2, 3, 4, 5, 6, 7, 9)), modifications[43])
        assertEquals(RemoveCandidates(7, 5, numbers(2, 3, 4, 5, 6, 9)), modifications[44])
        assertEquals(RemoveCandidates(7, 6, numbers(2, 3, 4, 5, 6, 8)), modifications[45])
        assertEquals(RemoveCandidates(8, 0, numbers(1, 2, 4, 6, 7, 9)), modifications[46])
        assertEquals(RemoveCandidates(8, 1, numbers(1, 2, 4, 6, 7, 8, 9)), modifications[47])
        assertEquals(RemoveCandidates(8, 2, numbers(2, 3, 4, 6, 7, 9)), modifications[48])
        assertEquals(RemoveCandidates(8, 4, numbers(2, 4, 7, 9)), modifications[49])
        assertEquals(RemoveCandidates(8, 6, numbers(3, 4, 5, 6, 8, 9)), modifications[50])
        assertEquals(RemoveCandidates(8, 7, numbers(1, 2, 3, 4, 5, 7, 8, 9)), modifications[51])
        assertEquals(RemoveCandidates(8, 8, numbers(3, 4, 5, 8, 9)), modifications[52])
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

        val modifications = fillSolvedCells(finalBoard)
        assertEquals(2, modifications.size)
        assertEquals(SetValue(0, 7, SudokuNumber.NINE), modifications[0])
        assertEquals(SetValue(8, 7, SudokuNumber.SIX), modifications[1])
    }

    private fun Cell.setCandidates(vararg candidates: Int) {
        removeCandidates(EnumSet.complementOf(numbers(*candidates)))
    }
}