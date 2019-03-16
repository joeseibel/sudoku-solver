package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.assertRemoveCandidates
import sudokusolver.kotlin.assertSetValue
import sudokusolver.kotlin.createCellBoardFromSimpleString
import sudokusolver.kotlin.createCellBoardFromStringWithCandidates

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
        assertRemoveCandidates(modifications[0], 0, 0, 1, 4, 5, 8, 9)
        assertRemoveCandidates(modifications[1], 0, 1, 1, 2, 4, 5, 6, 8)
        assertRemoveCandidates(modifications[2], 0, 2, 1, 3, 4, 5, 6, 7, 8)
        assertRemoveCandidates(modifications[3], 0, 4, 1, 2, 5, 7, 9)
        assertRemoveCandidates(modifications[4], 0, 6, 1, 4, 5, 6, 7)
        assertRemoveCandidates(modifications[5], 0, 7, 1, 2, 3, 4, 5, 6, 7, 8)
        assertRemoveCandidates(modifications[6], 0, 8, 1, 3, 4, 5, 6, 7)
        assertRemoveCandidates(modifications[7], 1, 2, 1, 3, 4, 6, 7, 8)
        assertRemoveCandidates(modifications[8], 1, 3, 1, 2, 4, 5, 6, 7)
        assertRemoveCandidates(modifications[9], 1, 4, 1, 2, 4, 5, 6, 7, 9)
        assertRemoveCandidates(modifications[10], 1, 5, 1, 2, 4, 5, 6, 7, 9)
        assertRemoveCandidates(modifications[11], 1, 8, 1, 3, 4, 5, 6, 7)
        assertRemoveCandidates(modifications[12], 2, 0, 1, 2, 4, 8, 9)
        assertRemoveCandidates(modifications[13], 2, 2, 1, 2, 3, 4, 6, 7, 8)
        assertRemoveCandidates(modifications[14], 2, 3, 1, 2, 4, 5, 8)
        assertRemoveCandidates(modifications[15], 2, 4, 1, 2, 4, 5, 7, 8, 9)
        assertRemoveCandidates(modifications[16], 2, 7, 1, 2, 3, 4, 6, 7, 8)
        assertRemoveCandidates(modifications[17], 2, 8, 2, 3, 4, 5, 6, 7, 8)
        assertRemoveCandidates(modifications[18], 3, 0, 1, 3, 6, 7, 9)
        assertRemoveCandidates(modifications[19], 3, 3, 1, 2, 3, 4, 6, 7, 9)
        assertRemoveCandidates(modifications[20], 3, 5, 1, 2, 3, 5, 6, 7, 9)
        assertRemoveCandidates(modifications[21], 3, 6, 1, 2, 3, 4, 5, 6, 7)
        assertRemoveCandidates(modifications[22], 3, 8, 1, 2, 3, 5, 6, 7)
        assertRemoveCandidates(modifications[23], 4, 1, 1, 2, 3, 4, 6, 8, 9)
        assertRemoveCandidates(modifications[24], 4, 2, 1, 3, 6, 7, 9)
        assertRemoveCandidates(modifications[25], 4, 3, 1, 2, 3, 4, 7, 9)
        assertRemoveCandidates(modifications[26], 4, 4, 3, 7, 9)
        assertRemoveCandidates(modifications[27], 4, 5, 2, 3, 5, 7, 9)
        assertRemoveCandidates(modifications[28], 4, 6, 1, 2, 3, 4, 5, 6, 9)
        assertRemoveCandidates(modifications[29], 4, 7, 1, 2, 3, 5, 7, 8, 9)
        assertRemoveCandidates(modifications[30], 5, 0, 1, 2, 3, 5, 6, 9)
        assertRemoveCandidates(modifications[31], 5, 2, 1, 2, 3, 5, 6, 7, 9)
        assertRemoveCandidates(modifications[32], 5, 3, 1, 2, 4, 5, 7, 9)
        assertRemoveCandidates(modifications[33], 5, 5, 1, 2, 5, 7, 9)
        assertRemoveCandidates(modifications[34], 5, 8, 1, 2, 3, 5, 9)
        assertRemoveCandidates(modifications[35], 6, 0, 1, 2, 6, 7, 8, 9)
        assertRemoveCandidates(modifications[36], 6, 1, 1, 2, 4, 6, 7, 8)
        assertRemoveCandidates(modifications[37], 6, 4, 2, 4, 7, 8, 9)
        assertRemoveCandidates(modifications[38], 6, 5, 2, 4, 5, 7, 8, 9)
        assertRemoveCandidates(modifications[39], 6, 6, 2, 3, 4, 5, 6, 7, 8)
        assertRemoveCandidates(modifications[40], 6, 8, 2, 3, 5, 7, 8)
        assertRemoveCandidates(modifications[41], 7, 0, 1, 2, 3, 5, 6, 7, 9)
        assertRemoveCandidates(modifications[42], 7, 3, 1, 2, 3, 4, 5, 6, 9)
        assertRemoveCandidates(modifications[43], 7, 4, 2, 3, 4, 5, 6, 7, 9)
        assertRemoveCandidates(modifications[44], 7, 5, 2, 3, 4, 5, 6, 9)
        assertRemoveCandidates(modifications[45], 7, 6, 2, 3, 4, 5, 6, 8)
        assertRemoveCandidates(modifications[46], 8, 0, 1, 2, 4, 6, 7, 9)
        assertRemoveCandidates(modifications[47], 8, 1, 1, 2, 4, 6, 7, 8, 9)
        assertRemoveCandidates(modifications[48], 8, 2, 2, 3, 4, 6, 7, 9)
        assertRemoveCandidates(modifications[49], 8, 4, 2, 4, 7, 9)
        assertRemoveCandidates(modifications[50], 8, 6, 3, 4, 5, 6, 8, 9)
        assertRemoveCandidates(modifications[51], 8, 7, 1, 2, 3, 4, 5, 7, 8, 9)
        assertRemoveCandidates(modifications[52], 8, 8, 3, 4, 5, 8, 9)
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
        assertSetValue(modifications[0], 0, 7, 9)
        assertSetValue(modifications[1], 8, 7, 6)
    }
}