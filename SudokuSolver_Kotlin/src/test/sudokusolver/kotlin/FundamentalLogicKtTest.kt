package sudokusolver.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.logic.fillSolvedCells
import sudokusolver.kotlin.logic.pruneCandidates

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
        val modifications = pruneCandidates(createCellBoardFromSimpleString(board))

        assertEquals(53, modifications.size)
        assertEquals(RemoveCandidates(0, 0, sudokuNumbers(1, 4, 5, 8, 9)), modifications[0])
        assertEquals(RemoveCandidates(0, 1, sudokuNumbers(1, 2, 4, 5, 6, 8)), modifications[1])
        assertEquals(RemoveCandidates(0, 2, sudokuNumbers(1, 3, 4, 5, 6, 7, 8)), modifications[2])
        assertEquals(RemoveCandidates(0, 4, sudokuNumbers(1, 2, 5, 7, 9)), modifications[3])
        assertEquals(RemoveCandidates(0, 6, sudokuNumbers(1, 4, 5, 6, 7)), modifications[4])
        assertEquals(RemoveCandidates(0, 7, sudokuNumbers(1, 2, 3, 4, 5, 6, 7, 8)), modifications[5])
        assertEquals(RemoveCandidates(0, 8, sudokuNumbers(1, 3, 4, 5, 6, 7)), modifications[6])
        assertEquals(RemoveCandidates(1, 2, sudokuNumbers(1, 3, 4, 6, 7, 8)), modifications[7])
        assertEquals(RemoveCandidates(1, 3, sudokuNumbers(1, 2, 4, 5, 6, 7)), modifications[8])
        assertEquals(RemoveCandidates(1, 4, sudokuNumbers(1, 2, 4, 5, 6, 7, 9)), modifications[9])
        assertEquals(RemoveCandidates(1, 5, sudokuNumbers(1, 2, 4, 5, 6, 7, 9)), modifications[10])
        assertEquals(RemoveCandidates(1, 8, sudokuNumbers(1, 3, 4, 5, 6, 7)), modifications[11])
        assertEquals(RemoveCandidates(2, 0, sudokuNumbers(1, 2, 4, 8, 9)), modifications[12])
        assertEquals(RemoveCandidates(2, 2, sudokuNumbers(1, 2, 3, 4, 6, 7, 8)), modifications[13])
        assertEquals(RemoveCandidates(2, 3, sudokuNumbers(1, 2, 4, 5, 8)), modifications[14])
        assertEquals(RemoveCandidates(2, 4, sudokuNumbers(1, 2, 4, 5, 7, 8, 9)), modifications[15])
        assertEquals(RemoveCandidates(2, 7, sudokuNumbers(1, 2, 3, 4, 6, 7, 8)), modifications[16])
        assertEquals(RemoveCandidates(2, 8, sudokuNumbers(2, 3, 4, 5, 6, 7, 8)), modifications[17])
        assertEquals(RemoveCandidates(3, 0, sudokuNumbers(1, 3, 6, 7, 9)), modifications[18])
        assertEquals(RemoveCandidates(3, 3, sudokuNumbers(1, 2, 3, 4, 6, 7, 9)), modifications[19])
        assertEquals(RemoveCandidates(3, 5, sudokuNumbers(1, 2, 3, 5, 6, 7, 9)), modifications[20])
        assertEquals(RemoveCandidates(3, 6, sudokuNumbers(1, 2, 3, 4, 5, 6, 7)), modifications[21])
        assertEquals(RemoveCandidates(3, 8, sudokuNumbers(1, 2, 3, 5, 6, 7)), modifications[22])
        assertEquals(RemoveCandidates(4, 1, sudokuNumbers(1, 2, 3, 4, 6, 8, 9)), modifications[23])
        assertEquals(RemoveCandidates(4, 2, sudokuNumbers(1, 3, 6, 7, 9)), modifications[24])
        assertEquals(RemoveCandidates(4, 3, sudokuNumbers(1, 2, 3, 4, 7, 9)), modifications[25])
        assertEquals(RemoveCandidates(4, 4, sudokuNumbers(3, 7, 9)), modifications[26])
        assertEquals(RemoveCandidates(4, 5, sudokuNumbers(2, 3, 5, 7, 9)), modifications[27])
        assertEquals(RemoveCandidates(4, 6, sudokuNumbers(1, 2, 3, 4, 5, 6, 9)), modifications[28])
        assertEquals(RemoveCandidates(4, 7, sudokuNumbers(1, 2, 3, 5, 7, 8, 9)), modifications[29])
        assertEquals(RemoveCandidates(5, 0, sudokuNumbers(1, 2, 3, 5, 6, 9)), modifications[30])
        assertEquals(RemoveCandidates(5, 2, sudokuNumbers(1, 2, 3, 5, 6, 7, 9)), modifications[31])
        assertEquals(RemoveCandidates(5, 3, sudokuNumbers(1, 2, 4, 5, 7, 9)), modifications[32])
        assertEquals(RemoveCandidates(5, 5, sudokuNumbers(1, 2, 5, 7, 9)), modifications[33])
        assertEquals(RemoveCandidates(5, 8, sudokuNumbers(1, 2, 3, 5, 9)), modifications[34])
        assertEquals(RemoveCandidates(6, 0, sudokuNumbers(1, 2, 6, 7, 8, 9)), modifications[35])
        assertEquals(RemoveCandidates(6, 1, sudokuNumbers(1, 2, 4, 6, 7, 8)), modifications[36])
        assertEquals(RemoveCandidates(6, 4, sudokuNumbers(2, 4, 7, 8, 9)), modifications[37])
        assertEquals(RemoveCandidates(6, 5, sudokuNumbers(2, 4, 5, 7, 8, 9)), modifications[38])
        assertEquals(RemoveCandidates(6, 6, sudokuNumbers(2, 3, 4, 5, 6, 7, 8)), modifications[39])
        assertEquals(RemoveCandidates(6, 8, sudokuNumbers(2, 3, 5, 7, 8)), modifications[40])
        assertEquals(RemoveCandidates(7, 0, sudokuNumbers(1, 2, 3, 5, 6, 7, 9)), modifications[41])
        assertEquals(RemoveCandidates(7, 3, sudokuNumbers(1, 2, 3, 4, 5, 6, 9)), modifications[42])
        assertEquals(RemoveCandidates(7, 4, sudokuNumbers(2, 3, 4, 5, 6, 7, 9)), modifications[43])
        assertEquals(RemoveCandidates(7, 5, sudokuNumbers(2, 3, 4, 5, 6, 9)), modifications[44])
        assertEquals(RemoveCandidates(7, 6, sudokuNumbers(2, 3, 4, 5, 6, 8)), modifications[45])
        assertEquals(RemoveCandidates(8, 0, sudokuNumbers(1, 2, 4, 6, 7, 9)), modifications[46])
        assertEquals(RemoveCandidates(8, 1, sudokuNumbers(1, 2, 4, 6, 7, 8, 9)), modifications[47])
        assertEquals(RemoveCandidates(8, 2, sudokuNumbers(2, 3, 4, 6, 7, 9)), modifications[48])
        assertEquals(RemoveCandidates(8, 4, sudokuNumbers(2, 4, 7, 9)), modifications[49])
        assertEquals(RemoveCandidates(8, 6, sudokuNumbers(3, 4, 5, 6, 8, 9)), modifications[50])
        assertEquals(RemoveCandidates(8, 7, sudokuNumbers(1, 2, 3, 4, 5, 7, 8, 9)), modifications[51])
        assertEquals(RemoveCandidates(8, 8, sudokuNumbers(3, 4, 5, 8, 9)), modifications[52])
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
        val board = """
            {2367}{379}{29}1{3468}5{2389}{9}{289}
            14{259}{389}{38}{38}67{289}
            {3567}8{59}{3679}{36}24{59}{19}
            {2458}63{58}7{48}{89}1{489}
            9{57}{2458}{568}{124568}{1468}{78}{46}3
            {478}1{48}{368}9{3468}52{4678}
            {345}{359}72{1356}{136}{19}8{1469}
            {48}26{78}{18}{178}{179}35
            {358}{35}{158}4{13568}9{127}{6}{1267}
        """.trimIndent().replace("\n", "")
        val modifications = fillSolvedCells(createCellBoardFromStringWithCandidates(board))

        assertEquals(2, modifications.size)
        assertEquals(SetValue(0, 7, SudokuNumber.NINE), modifications[0])
        assertEquals(SetValue(8, 7, SudokuNumber.SIX), modifications[1])
    }
}