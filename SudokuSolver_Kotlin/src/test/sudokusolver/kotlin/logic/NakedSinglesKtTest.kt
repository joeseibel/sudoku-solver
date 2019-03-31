package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.assertSetValue
import sudokusolver.kotlin.createCellBoardFromStringWithCandidates

internal class NakedSinglesKtTest {
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
    fun testNakedSingles() {
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
        val modifications = nakedSingles(createCellBoardFromStringWithCandidates(board)).sorted()

        assertEquals(2, modifications.size)
        assertSetValue(modifications[0], 0, 7, 9)
        assertSetValue(modifications[1], 8, 7, 6)
    }
}