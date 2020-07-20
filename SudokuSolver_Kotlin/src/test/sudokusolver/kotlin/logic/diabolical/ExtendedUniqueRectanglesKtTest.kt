package sudokusolver.kotlin.logic.diabolical

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class ExtendedUniqueRectanglesKtTest {
    /*
     * 9 0 7 | 0 2 4 | 3 0 5
     * 8 4 2 | 3 6 5 | 9 1 7
     * 0 3 0 | 9 0 7 | 4 0 0
     * ------+-------+------
     * 0 0 4 | 0 0 9 | 6 0 0
     * 0 0 0 | 2 4 6 | 0 0 9
     * 0 0 9 | 0 0 0 | 0 4 0
     * ------+-------+------
     * 0 0 0 | 4 9 2 | 8 7 6
     * 7 9 6 | 0 5 0 | 2 3 4
     * 4 2 8 | 6 7 3 | 5 9 1
     */
    @Test
    fun extendedUniqueRectanglesTest1() {
        val board = """
            9{16}7{18}243{68}5
            842365917
            {156}3{15}9{18}74{268}{28}
            {125}{1578}4{57}{138}96{258}{238}
            {135}{178}{13}246{17}{58}9
            {256}{1567}9{57}{138}{18}{17}4{238}
            {135}{15}{135}492876
            796{18}5{18}234
            428673591
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 0, 1, 5)
        )
        assertLogicalSolution(expected, board, ::extendedUniqueRectangles)
    }

    /*
     * 0 6 3 | 8 2 0 | 0 0 0
     * 7 0 0 | 0 0 9 | 2 0 6
     * 2 1 9 | 0 0 7 | 3 8 0
     * ------+-------+------
     * 0 3 2 | 0 9 0 | 8 0 0
     * 9 0 0 | 0 0 0 | 0 0 2
     * 0 0 1 | 0 0 2 | 5 3 9
     * ------+-------+------
     * 1 2 4 | 9 7 8 | 6 5 3
     * 8 5 6 | 2 0 0 | 0 0 7
     * 3 9 7 | 0 4 0 | 1 2 8
     */
    @Test
    fun extendedUniqueRectanglesTest2() {
        val board = """
            {45}6382{145}{79}{79}{145}
            7{48}{58}{1345}{135}92{14}6
            219{456}{56}738{45}
            {456}32{14567}9{1456}8{1467}{14}
            9{478}{58}{134567}{13568}{13456}{47}{1467}2
            {46}{478}1{467}{68}2539
            124978653
            8562{13}{13}{49}{49}7
            397{56}4{56}128
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(4, 7, 4, 7)
        )
        assertLogicalSolution(expected, board, ::extendedUniqueRectangles)
    }

    /*
     * 0 9 0 | 8 0 1 | 5 2 0
     * 8 5 1 | 0 9 2 | 6 0 4
     * 0 2 0 | 0 0 5 | 9 1 8
     * ------+-------+------
     * 0 0 9 | 6 5 0 | 0 4 0
     * 4 0 0 | 0 0 0 | 0 0 6
     * 0 6 0 | 0 8 4 | 1 0 0
     * ------+-------+------
     * 0 7 0 | 0 0 6 | 4 8 0
     * 0 0 6 | 4 7 8 | 2 0 5
     * 0 4 8 | 5 0 9 | 0 6 0
     */
    @Test
    fun extendedUniqueRectanglesTest3() {
        val board = """
            {367}9{347}8{46}152{37}
            851{37}926{37}4
            {367}2{347}{37}{46}5918
            {1237}{138}965{37}{378}4{237}
            4{38}{25}{129}{12}{37}{378}{59}6
            {2357}6{2357}{29}841{3579}{2379}
            {2359}7{235}{12}{123}648{139}
            {139}{13}64782{39}5
            {23}485{123}9{37}6{137}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 1, 3, 8)
        )
        assertLogicalSolution(expected, board, ::extendedUniqueRectangles)
    }
}