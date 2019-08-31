package sudokusolver.kotlin.logic.simple

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class NakedPairsKtTest {
    /*
     * 4 0 0 | 0 0 0 | 9 3 8
     * 0 3 2 | 0 9 4 | 1 0 0
     * 0 9 5 | 3 0 0 | 2 4 0
     * ------+-------+------
     * 3 7 0 | 6 0 9 | 0 0 4
     * 5 2 9 | 0 0 1 | 6 7 3
     * 6 0 4 | 7 0 3 | 0 9 0
     * ------+-------+------
     * 9 5 7 | 0 0 8 | 3 0 0
     * 0 0 3 | 9 0 0 | 4 0 0
     * 2 4 0 | 0 3 0 | 7 0 9
     */
    @Test
    fun testNakedPairs1() {
        val board = """
            4{16}{16}{125}{12567}{2567}938
            {78}32{58}941{56}{567}
            {178}953{1678}{67}24{67}
            37{18}6{258}9{58}{1258}4
            529{48}{48}1673
            6{18}47{258}3{58}9{125}
            957{124}{1246}83{126}{126}
            {18}{168}39{12567}{2567}4{12568}{1256}
            24{168}{15}3{56}7{1568}9
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 3, 1),
            RemoveCandidates(0, 4, 1, 6),
            RemoveCandidates(0, 5, 6),
            RemoveCandidates(2, 0, 1, 7),
            RemoveCandidates(2, 4, 6, 7),
            RemoveCandidates(3, 4, 8),
            RemoveCandidates(3, 7, 5, 8),
            RemoveCandidates(5, 4, 8),
            RemoveCandidates(5, 8, 5)
        )
        assertLogicalSolution(expected, board, ::nakedPairs)
    }

    /*
     * 0 8 0 | 0 9 0 | 0 3 0
     * 0 3 0 | 0 0 0 | 0 6 9
     * 9 0 2 | 0 6 3 | 1 5 8
     * ------+-------+------
     * 0 2 0 | 8 0 4 | 5 9 0
     * 8 5 1 | 9 0 7 | 0 4 6
     * 3 9 4 | 6 0 5 | 8 7 0
     * ------+-------+------
     * 5 6 3 | 0 4 0 | 9 8 7
     * 2 0 0 | 0 0 0 | 0 1 5
     * 0 1 0 | 0 5 0 | 0 2 0
     */
    @Test
    fun testNakedPairs2() {
        val board = """
            {1467}8{567}{12457}9{12}{247}3{24}
            {147}3{57}{12457}{1278}{128}{247}69
            9{47}2{47}63158
            {67}2{67}8{13}459{13}
            8519{23}7{23}46
            3946{12}587{12}
            563{12}4{12}987
            2{47}{789}{37}{378}{689}{346}15
            {47}1{789}{37}5{689}{346}2{34}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 3, 7),
            RemoveCandidates(1, 3, 7),
            RemoveCandidates(1, 5, 1, 2),
            RemoveCandidates(2, 3, 7),
            RemoveCandidates(7, 2, 7),
            RemoveCandidates(7, 4, 3, 7),
            RemoveCandidates(8, 2, 7)
        )
        assertLogicalSolution(expected, board, ::nakedPairs)
    }
}