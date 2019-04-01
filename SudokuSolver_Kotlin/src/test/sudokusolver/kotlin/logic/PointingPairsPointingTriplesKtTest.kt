package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.createCellBoardFromStringWithCandidates

internal class PointingPairsPointingTriplesKtTest {
    /*
     * 0 1 7 | 9 0 3 | 6 0 0
     * 0 0 0 | 0 8 0 | 0 0 0
     * 9 0 0 | 0 0 0 | 5 0 7
     * ------+-------+------
     * 0 7 2 | 0 1 0 | 4 3 0
     * 0 0 0 | 4 0 2 | 0 7 0
     * 0 6 4 | 3 7 0 | 2 5 0
     * ------+-------+------
     * 7 0 1 | 0 0 0 | 0 6 5
     * 0 0 0 | 0 3 0 | 0 0 0
     * 0 0 5 | 6 0 1 | 7 2 0
     */
    @Test
    fun testPointingPairsPointingTriples1() {
        val board = """
            {2458}179{245}36{48}{248}
            {23456}{2345}{36}{1257}8{57}{139}{149}{12349}
            9{2348}{368}{12}{246}{46}5{148}7
            {58}72{58}1{69}43{69}
            {1358}{3589}{389}4{569}2{189}7{1689}
            {18}6437{89}25{189}
            7{23489}1{28}{249}{489}{389}65
            {2468}{2489}{689}{57}3{57}{189}{1489}{1489}
            {348}{3489}56{49}172{3489}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 0, 3),
            RemoveCandidates(1, 1, 3),
            RemoveCandidates(1, 2, 3),
            RemoveCandidates(2, 2, 6),
            RemoveCandidates(4, 4, 9),
            RemoveCandidates(4, 6, 9),
            RemoveCandidates(4, 8, 9),
            RemoveCandidates(6, 1, 2, 8),
            RemoveCandidates(6, 6, 8)
        )
        assertIterableEquals(
            expected,
            pointingPairsPointingTriples(createCellBoardFromStringWithCandidates(board)).sorted()
        )
    }

    /*
     * 0 3 2 | 0 0 6 | 1 0 0
     * 4 1 0 | 0 0 0 | 0 0 0
     * 0 0 0 | 9 0 1 | 0 0 0
     * ------+-------+------
     * 5 0 0 | 0 9 0 | 0 0 4
     * 0 6 0 | 0 0 0 | 0 7 1
     * 3 0 0 | 0 2 0 | 0 0 5
     * ------+-------+------
     * 0 0 0 | 5 0 8 | 0 0 0
     * 0 0 0 | 0 0 0 | 5 1 9
     * 0 5 7 | 0 0 9 | 8 6 0
     */
    @Test
    fun testPointingPairsPointingTriples2() {
        val board = """
            {789}32{478}{4578}61{4589}{78}
            41{5689}{2378}{3578}{2357}{23679}{23589}{23678}
            {678}{78}{568}9{34578}1{23467}{23458}{23678}
            5{278}{18}{16}9{37}{236}{238}4
            {289}6{489}{348}{3458}{345}{239}71
            3{4789}{1489}{16}2{47}{69}{89}5
            {1269}{249}{13469}5{13467}8{2347}{234}{237}
            {268}{248}{3468}{2347}{3467}{2347}519
            {12}57{234}{134}986{23}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 7, 8),
            RemoveCandidates(1, 5, 7),
            RemoveCandidates(1, 6, 2, 6),
            RemoveCandidates(1, 7, 2, 8),
            RemoveCandidates(1, 8, 2),
            RemoveCandidates(2, 1, 7),
            RemoveCandidates(2, 6, 6),
            RemoveCandidates(2, 7, 8),
            RemoveCandidates(4, 0, 8),
            RemoveCandidates(4, 2, 8),
            RemoveCandidates(6, 1, 4),
            RemoveCandidates(6, 2, 1, 4),
            RemoveCandidates(6, 4, 4, 7),
            RemoveCandidates(7, 5, 7)
        )
        assertIterableEquals(
            expected,
            pointingPairsPointingTriples(createCellBoardFromStringWithCandidates(board)).sorted()
        )
    }

    /*
     * 9 3 0 | 0 5 0 | 0 0 0
     * 2 0 0 | 6 3 0 | 0 9 5
     * 8 5 6 | 0 0 2 | 0 0 0
     * ------+-------+------
     * 0 0 3 | 1 8 0 | 5 7 0
     * 0 0 5 | 0 2 0 | 9 8 0
     * 0 8 0 | 0 0 5 | 0 0 0
     * ------+-------+------
     * 0 0 0 | 8 0 0 | 1 5 9
     * 5 0 8 | 2 1 0 | 0 0 4
     * 0 0 0 | 5 6 0 | 0 0 8
     */
    @Test
    fun testPointingPairsPointingTriples3() {
        val board = """
            93{147}{47}5{18}{24678}{1246}{1267}
            2{147}{147}63{18}{478}95
            856{479}{479}2{347}{134}{137}
            {46}{29}318{469}57{26}
            {1467}{1467}5{347}2{3467}98{136}
            {1467}8{29}{3479}{479}5{2346}{12346}{1236}
            {3467}{2467}{247}8{47}{347}159
            5{679}821{379}{367}{36}4
            {1347}{12479}{12479}56{3479}{237}{23}8
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 6, 7),
            RemoveCandidates(1, 6, 7),
            RemoveCandidates(2, 6, 7),
            RemoveCandidates(3, 5, 9),
            RemoveCandidates(4, 5, 3),
            RemoveCandidates(5, 0, 4),
            RemoveCandidates(5, 3, 4),
            RemoveCandidates(5, 4, 4),
            RemoveCandidates(7, 1, 6),
            RemoveCandidates(8, 1, 2),
            RemoveCandidates(8, 2, 2)
        )
        assertIterableEquals(
            expected,
            pointingPairsPointingTriples(createCellBoardFromStringWithCandidates(board)).sorted()
        )
    }
}