package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.createCellBoardFromStringWithCandidates

internal class BoxLineReductionKtTest {
    /*
     * 0 1 6 | 0 0 7 | 8 0 3
     * 0 9 2 | 8 0 0 | 0 0 0
     * 8 7 0 | 0 0 1 | 2 6 0
     * ------+-------+------
     * 0 4 8 | 0 0 0 | 3 0 0
     * 6 5 0 | 0 0 9 | 0 8 2
     * 0 3 9 | 0 0 0 | 6 5 0
     * ------+-------+------
     * 0 6 0 | 9 0 0 | 0 2 0
     * 0 8 0 | 0 0 2 | 9 3 6
     * 9 2 4 | 6 0 0 | 5 1 0
     */
    @Test
    fun testBoxLineReduction1() {
        val board = """
            {45}16{245}{2459}78{49}3
            {345}928{3456}{3456}{147}{47}{1457}
            87{35}{345}{3459}126{459}
            {127}48{1257}{12567}{56}3{79}{179}
            65{17}{1347}{1347}9{147}82
            {127}39{1247}{12478}{48}65{147}
            {1357}6{1357}9{1578}{58}{47}2{478}
            {157}8{157}{1457}{1457}2936
            9246{378}{38}51{78}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 6, 4),
            RemoveCandidates(1, 8, 4),
            RemoveCandidates(2, 8, 4)
        )
        assertIterableEquals(expected, boxLineReduction(createCellBoardFromStringWithCandidates(board)).sorted())
    }

    /*
     * 0 2 0 | 9 4 3 | 7 1 5
     * 9 0 4 | 0 0 0 | 6 0 0
     * 7 5 0 | 0 0 0 | 0 4 0
     * ------+-------+------
     * 5 0 0 | 4 8 0 | 0 0 0
     * 2 0 0 | 0 0 0 | 4 5 3
     * 4 0 0 | 3 5 2 | 0 0 0
     * ------+-------+------
     * 0 4 2 | 0 0 0 | 0 8 1
     * 0 0 5 | 0 0 4 | 2 6 0
     * 0 9 0 | 2 0 8 | 5 0 4
     */
    @Test
    fun testBoxLineReduction2() {
        val board = """
            {68}2{68}943715
            9{13}4{1578}{127}{157}6{23}{28}
            75{13}{168}{126}{16}{389}4{289}
            5{1367}{13679}48{1679}{19}{279}{2679}
            2{1678}{16789}{167}{1679}{1679}453
            4{167}{1679}352{189}{79}{6789}
            {36}42{567}{3679}{5679}{39}81
            {138}{1378}5{17}{1379}426{79}
            {136}9{1367}2{1367}85{37}4
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 2, 6),
            RemoveCandidates(3, 6, 9),
            RemoveCandidates(3, 8, 9),
            RemoveCandidates(4, 2, 6),
            RemoveCandidates(5, 2, 6),
            RemoveCandidates(5, 6, 9),
            RemoveCandidates(5, 8, 9),
            RemoveCandidates(7, 1, 1, 3),
            RemoveCandidates(7, 3, 7),
            RemoveCandidates(7, 4, 7),
            RemoveCandidates(8, 2, 1, 3),
            RemoveCandidates(8, 4, 7)
        )
        assertIterableEquals(expected, boxLineReduction(createCellBoardFromStringWithCandidates(board)).sorted())
    }
}