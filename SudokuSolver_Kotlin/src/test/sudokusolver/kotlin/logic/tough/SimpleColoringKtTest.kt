package sudokusolver.kotlin.logic.tough

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.parseCellsWithCandidates

internal class SimpleColoringKtTest {
    /*
     * 0 0 7 | 0 8 3 | 6 0 0
     * 0 3 9 | 7 0 6 | 8 0 0
     * 8 2 6 | 4 1 9 | 7 5 3
     * ------+-------+------
     * 6 4 0 | 1 9 0 | 3 8 7
     * 0 8 0 | 3 6 7 | 0 0 0
     * 0 7 3 | 0 4 8 | 0 6 0
     * ------+-------+------
     * 3 9 0 | 8 7 0 | 0 2 6
     * 7 6 4 | 9 0 0 | 1 3 8
     * 2 0 8 | 6 3 0 | 9 7 0
     */
    @Test
    fun testSimpleColoringRule2Test1() {
        val board = """
            {145}{15}7{25}836{149}{1249}
            {145}397{25}68{14}{124}
            826419753
            64{25}19{25}387
            {159}8{12}367{245}{149}{1459}
            {19}73{25}48{25}6{19}
            39{15}87{14}{45}26
            7649{25}{25}138
            2{15}863{14}97{45}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 1, 5),
            RemoveCandidates(0, 3, 5),
            RemoveCandidates(1, 0, 5),
            RemoveCandidates(3, 5, 5),
            RemoveCandidates(4, 0, 5),
            RemoveCandidates(5, 6, 5),
            RemoveCandidates(6, 2, 5),
            RemoveCandidates(7, 4, 5),
            RemoveCandidates(8, 8, 5)
        )
        assertIterableEquals(expected, simpleColoringRule2(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 2 0 0 | 0 4 1 | 0 5 6
     * 4 0 5 | 6 0 2 | 0 1 0
     * 0 1 6 | 0 9 5 | 0 0 4
     * ------+-------+------
     * 3 5 0 | 1 2 9 | 6 4 0
     * 1 4 2 | 0 6 0 | 5 9 0
     * 0 6 9 | 5 0 4 | 0 0 1
     * ------+-------+------
     * 5 8 4 | 2 1 6 | 3 7 9
     * 9 2 0 | 4 0 8 | 1 6 5
     * 6 0 1 | 9 5 0 | 4 8 2
     */
    @Test
    fun testSimpleColoringRule2Test2() {
        val board = """
            2{79}{38}{38}41{79}56
            4{379}56{78}2{789}1{37}
            {78}16{37}95{278}{23}4
            35{78}12964{78}
            142{78}6{37}59{38}
            {78}695{38}4{27}{23}1
            584216379
            92{37}4{37}8165
            6{37}195{37}482
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 8, 7),
            RemoveCandidates(2, 0, 7),
            RemoveCandidates(2, 3, 7),
            RemoveCandidates(3, 2, 7),
            RemoveCandidates(4, 5, 7),
            RemoveCandidates(5, 6, 7),
            RemoveCandidates(7, 4, 7),
            RemoveCandidates(8, 1, 7)
        )
        assertIterableEquals(expected, simpleColoringRule2(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 4 0 0 | 8 0 6 | 0 1 3
     * 0 8 6 | 0 1 3 | 4 0 9
     * 0 0 1 | 0 4 5 | 8 6 7
     * ------+-------+------
     * 0 1 0 | 4 6 8 | 0 9 2
     * 0 0 8 | 3 0 1 | 6 4 5
     * 6 4 0 | 0 5 0 | 0 8 1
     * ------+-------+------
     * 1 5 4 | 6 0 0 | 9 0 8
     * 9 0 7 | 5 8 4 | 1 0 6
     * 8 6 0 | 1 0 9 | 0 0 4
     */
    @Test
    fun testSimpleColoringRule2Test3() {
        val board = """
            4{279}{259}8{279}6{25}13
            {257}86{27}134{25}9
            {23}{239}1{29}45867
            {357}1{35}468{37}92
            {27}{279}83{279}1645
            64{239}{279}5{27}{37}81
            1546{237}{27}9{237}8
            9{23}75841{23}6
            86{23}1{237}9{25}{2357}4
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 2, 9),
            RemoveCandidates(0, 4, 9),
            RemoveCandidates(2, 1, 9),
            RemoveCandidates(4, 1, 9),
            RemoveCandidates(5, 3, 9)
        )
        assertIterableEquals(expected, simpleColoringRule2(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 2 8 9 | 0 0 0 | 3 7 5
     * 3 6 4 | 0 9 0 | 8 1 2
     * 5 1 7 | 2 8 3 | 9 6 4
     * ------+-------+------
     * 8 9 3 | 0 2 0 | 6 0 1
     * 1 4 5 | 8 3 6 | 7 2 9
     * 7 2 6 | 0 0 0 | 0 8 3
     * ------+-------+------
     * 4 5 1 | 3 7 8 | 2 9 6
     * 0 7 2 | 0 1 0 | 0 3 8
     * 0 3 8 | 0 0 2 | 1 0 7
     */
    @Test
    fun testSimpleColoringRule2Test4() {
        val board = """
            289{16}{46}{14}375
            364{57}9{57}812
            517283964
            893{457}2{457}6{45}1
            145836729
            726{19}{45}{19}{45}83
            451378296
            {69}72{4569}1{459}{45}38
            {69}38{4569}{56}21{45}7
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(5, 6, 5),
            RemoveCandidates(8, 4, 5),
            RemoveCandidates(8, 7, 5)
        )
        assertIterableEquals(expected, simpleColoringRule2(parseCellsWithCandidates(board)).sorted())
    }
}