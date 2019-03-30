package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.assertRemoveCandidates
import sudokusolver.kotlin.createCellBoardFromStringWithCandidates

internal class HiddenTriplesKtTest {
    /*
     * 0 0 0 | 0 0 1 | 0 3 0
     * 2 3 1 | 0 9 0 | 0 0 0
     * 0 6 5 | 0 0 3 | 1 0 0
     * ------+-------+------
     * 6 7 8 | 9 2 4 | 3 0 0
     * 1 0 3 | 0 5 0 | 0 0 6
     * 0 0 0 | 1 3 6 | 7 0 0
     * ------+-------+------
     * 0 0 9 | 3 6 0 | 5 7 0
     * 0 0 6 | 0 1 9 | 8 4 3
     * 3 0 0 | 0 0 0 | 0 0 0
     */
    @Test
    fun testHiddenTriples() {
        val board = """
            {4789}{489}{47}{245678}{478}1{2469}3{245789}
            231{45678}9{578}{46}{56}{4578}
            {4789}65{2478}{478}31{289}{24789}
            6789243{15}{15}
            1{249}3{78}5{78}{249}{29}6
            {459}{2459}{24}1367{289}{2489}
            {48}{1248}936{28}57{12}
            {57}{25}6{257}19843
            3{12458}{247}{24578}{478}{2578}{269}{16}{129}
        """.trimIndent().replace("\n", "")
        val modifications = hiddenTriples(createCellBoardFromStringWithCandidates(board))

        assertEquals(3, modifications.size)
        assertRemoveCandidates(modifications[0], 0, 3, 4, 7, 8)
        assertRemoveCandidates(modifications[1], 0, 6, 4, 9)
        assertRemoveCandidates(modifications[2], 0, 8, 4, 7, 8, 9)
    }
}