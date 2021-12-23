package sudokusolver.kotlin.logic.extreme

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class SueDeCoqKtTest {
    @Test
    fun test1() {
        val board = """
            {47}1{34}958{346}{467}2
            {289}{28}634751{89}
            5{378}{3489}621{348}{4789}{78}
            {1289}{258}7{148}6{25}{1489}3{189}
            6{238}{12389}{1478}{78}{23}{148}{479}5
            {18}4{35}{178}9{35}2{678}{1678}
            {1278}{2567}{125}{78}39{16}{268}4
            {248}9{248}5167{28}3
            3{678}{18}2{78}4{1689}5{1689}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 1, 8),
            RemoveCandidates(4, 2, 3),
            RemoveCandidates(6, 1, 2),
            RemoveCandidates(8, 1, 8)
        )
        assertLogicalSolution(expected, board, ::sueDeCoq)
    }

    @Test
    fun test2() {
        val board = """
            15{78}432{78}69
            9{27}4186{23}{237}5
            {26}{268}3{59}7{59}{128}{128}4
            {567}{69}2{3568}{16}{1578}{3689}4{137}
            {4567}{68}{158}{3568}9{134578}{136}{1378}2
            {467}3{189}{68}2{478}5{1789}{17}
            {25}{279}{59}{3689}{16}{1389}4{137}{137}
            34675{19}{129}{129}8
            81{79}24{39}{379}56
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 6, 3),
            RemoveCandidates(4, 0, 6),
            RemoveCandidates(4, 2, 8),
            RemoveCandidates(4, 3, 6, 8),
            RemoveCandidates(4, 5, 8),
            RemoveCandidates(5, 7, 1, 7)
        )
        assertLogicalSolution(expected, board, ::sueDeCoq)
    }

    @Test
    fun test3() {
        val board = """
            15{78}432{78}69
            9{27}4186{23}{237}5
            {26}{268}3{59}7{59}{128}{128}4
            {567}{69}2{3568}{16}{1578}{3689}4{137}
            {4567}{68}{158}{35}9{13457}{136}{1378}2
            {467}3{189}{68}2{478}5{1789}{17}
            {25}{279}{59}{3689}{16}{1389}4{137}{137}
            34675{19}{129}{129}8
            81{79}24{39}{379}56
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 6, 3),
            RemoveCandidates(4, 0, 6),
            RemoveCandidates(4, 2, 8),
            RemoveCandidates(5, 7, 1, 7)
        )
        assertLogicalSolution(expected, board, ::sueDeCoq)
    }
}