package sudokusolver.kotlin.logic.diabolical

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class HiddenUniqueRectanglesKtTest {
    /*
     * 1 0 9 | 5 7 0 | 3 0 0
     * 0 7 0 | 3 9 0 | 0 1 0
     * 0 0 3 | 0 1 0 | 5 9 7
     * ------+-------+------
     * 0 8 0 | 7 4 3 | 0 0 0
     * 4 9 2 | 0 5 0 | 7 8 3
     * 7 3 0 | 2 8 9 | 0 4 0
     * ------+-------+------
     * 3 1 7 | 0 2 0 | 4 0 0
     * 2 6 0 | 0 3 0 | 0 7 0
     * 9 5 0 | 0 6 7 | 2 3 1
     */
    @Test
    fun hiddenUniqueRectanglesType1Test1() {
        val board = """
            1{24}957{2468}3{26}{2468}
            {568}7{56}39{24}{68}1{24}
            {68}{24}3{468}1{2468}597
            {56}8{156}743{169}{256}{2569}
            492{16}5{16}783
            73{156}289{16}4{56}
            317{89}2{58}4{56}{5689}
            26{48}{19}3{145}{89}7{589}
            95{48}{48}67231
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 5, 2, 4),
            RemoveCandidates(3, 2, 6)
        )
        assertLogicalSolution(expected, board, ::hiddenUniqueRectangles)
    }

    /*
     * 5 1 8 | 4 7 2 | 6 3 9
     * 3 0 6 | 8 5 9 | 0 0 4
     * 4 0 9 | 3 1 6 | 0 0 0
     * ------+-------+------
     * 9 4 5 | 6 2 0 | 3 0 0
     * 8 6 1 | 0 3 4 | 0 0 5
     * 7 3 2 | 0 8 5 | 0 0 6
     * ------+-------+------
     * 6 5 0 | 0 9 0 | 8 0 3
     * 2 9 3 | 0 4 8 | 0 6 1
     * 1 8 0 | 0 6 3 | 0 0 0
     */
    @Test
    fun hiddenUniqueRectanglesType1Test2() {
        val board = """
            518472639
            3{27}6859{127}{127}4
            4{27}9316{257}{2578}{278}
            94562{17}3{178}{78}
            861{79}34{279}{279}5
            732{19}85{149}{149}6
            65{47}{127}9{17}8{247}3
            293{57}48{57}61
            18{47}{257}63{24579}{24579}{27}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 7, 7),
            RemoveCandidates(8, 6, 7),
            RemoveCandidates(8, 7, 7)
        )
        assertLogicalSolution(expected, board, ::hiddenUniqueRectangles)
    }

    /*
     * 5 0 0 | 2 9 1 | 8 3 6
     * 0 3 0 | 4 7 5 | 0 1 0
     * 0 0 9 | 3 8 6 | 4 5 7
     * ------+-------+------
     * 0 5 0 | 1 4 3 | 0 0 0
     * 4 0 0 | 7 5 9 | 0 0 1
     * 0 0 0 | 8 6 2 | 0 4 5
     * ------+-------+------
     * 3 0 0 | 0 2 0 | 1 7 0
     * 0 8 0 | 9 3 7 | 0 2 4
     * 7 0 2 | 0 1 0 | 0 0 3
     */
    @Test
    fun hiddenUniqueRectanglesType2Test1() {
        val board = """
            5{47}{47}291836
            {68}3{68}475{29}1{29}
            {12}{12}9386457
            {2689}5{678}143{2679}{689}{289}
            4{26}{368}759{236}{68}1
            {19}{17}{137}862{379}45
            3{469}{456}{56}2{48}17{89}
            {16}8{156}937{56}24
            7{49}2{56}1{48}{569}{689}3
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 2, 6),
            RemoveCandidates(3, 6, 9)
        )
        assertLogicalSolution(expected, board, ::hiddenUniqueRectangles)
    }

    /*
     * 5 1 8 | 4 7 2 | 6 3 9
     * 3 0 6 | 8 5 9 | 0 0 4
     * 4 0 9 | 3 1 6 | 0 0 0
     * ------+-------+------
     * 9 4 5 | 6 2 0 | 3 0 0
     * 8 6 1 | 0 3 4 | 0 0 5
     * 7 3 2 | 0 8 5 | 0 0 6
     * ------+-------+------
     * 6 5 0 | 0 9 0 | 8 0 3
     * 2 9 3 | 0 4 8 | 0 6 1
     * 1 8 0 | 0 6 3 | 0 0 0
     */
    @Test
    fun hiddenUniqueRectanglesType2Test2() {
        val board = """
            518472639
            3{27}6859{127}{127}4
            4{27}9316{257}{258}{278}
            94562{17}3{178}{78}
            861{79}34{279}{279}5
            732{19}85{149}{149}6
            65{47}{127}9{17}8{247}3
            293{57}48{57}61
            18{47}{257}63{24579}{24579}{27}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(8, 6, 7),
            RemoveCandidates(8, 7, 7)
        )
        assertLogicalSolution(expected, board, ::hiddenUniqueRectangles)
    }

    /*
     * 0 2 0 | 5 8 0 | 0 3 0
     * 3 5 0 | 0 0 0 | 0 8 4
     * 0 8 6 | 7 0 0 | 0 2 0
     * ------+-------+------
     * 0 4 8 | 0 9 0 | 1 5 6
     * 5 0 0 | 6 0 8 | 0 4 0
     * 9 6 3 | 0 5 0 | 2 7 8
     * ------+-------+------
     * 0 9 0 | 0 6 5 | 8 1 0
     * 6 0 0 | 8 0 0 | 0 9 2
     * 8 3 0 | 0 0 0 | 0 6 0
     */
    @Test
    fun hiddenUniqueRectanglesType2BTest1() {
        val board = """
            {147}2{479}58{469}{679}3{179}
            35{79}{129}{12}{1269}{679}84
            {14}867{34}{349}{59}2{159}
            {27}48{23}9{237}156
            5{17}{127}6{27}8{39}4{39}
            963{14}5{14}278
            {247}9{247}{234}6581{37}
            6{17}{1457}8{1347}{134}{3457}92
            83{12457}{1249}{1247}{12479}{457}6{57}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(7, 2, 7),
            RemoveCandidates(7, 5, 4)
        )
        assertLogicalSolution(expected, board, ::hiddenUniqueRectangles)
    }

    /*
     * 5 1 8 | 4 7 2 | 6 3 9
     * 3 0 6 | 8 5 9 | 0 0 4
     * 4 0 9 | 3 1 6 | 0 0 0
     * ------+-------+------
     * 9 4 5 | 6 2 0 | 3 0 0
     * 8 6 1 | 0 3 4 | 0 0 5
     * 7 3 2 | 0 8 5 | 0 0 6
     * ------+-------+------
     * 6 5 0 | 0 9 0 | 8 0 3
     * 2 9 3 | 0 4 8 | 0 6 1
     * 1 8 0 | 0 6 3 | 0 0 0
     */
    @Test
    fun hiddenUniqueRectanglesType2BTest2() {
        val board = """
            518472639
            3{27}6859{127}{127}4
            4{27}9316{257}{258}{278}
            94562{17}3{178}{78}
            861{79}34{279}{279}5
            732{19}85{149}{149}6
            65{47}{127}9{17}8{247}3
            293{57}48{57}61
            18{47}{257}63{24579}{2459}{27}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(8, 6, 7)
        )
        assertLogicalSolution(expected, board, ::hiddenUniqueRectangles)
    }
}