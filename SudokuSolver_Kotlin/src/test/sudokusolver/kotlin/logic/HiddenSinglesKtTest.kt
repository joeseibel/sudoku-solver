package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.createCellBoardFromStringWithCandidates

internal class HiddenSinglesKtTest {
    /*
     * 2 0 0 | 0 7 0 | 0 3 8
     * 0 0 0 | 0 0 6 | 0 7 0
     * 3 0 0 | 0 4 0 | 6 0 0
     * ------+-------+------
     * 0 0 8 | 0 2 0 | 7 0 0
     * 1 0 0 | 0 0 0 | 0 0 6
     * 0 0 7 | 0 3 0 | 4 0 0
     * ------+-------+------
     * 0 0 4 | 0 8 0 | 0 0 9
     * 0 6 0 | 4 0 0 | 0 0 0
     * 9 1 0 | 0 6 0 | 0 0 2
     */
    @Test
    fun testHiddenSingles() {
        val board = """
            2{459}{1569}{159}7{159}{159}38
            {458}{4589}{159}{123589}{159}6{1259}7{145}
            3{5789}{159}{12589}4{12589}6{1259}{15}
            {456}{3459}8{1569}2{1459}7{159}{135}
            1{23459}{2359}{5789}{59}{45789}{23589}{2589}6
            {56}{259}7{15689}3{1589}4{12589}{15}
            {57}{2357}4{12357}8{12357}{135}{156}9
            {578}6{235}4{159}{123579}{1358}{158}{1357}
            91{35}{357}6{357}{358}{458}2
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            SetValue(0, 1, 4),
            SetValue(0, 2, 6),
            SetValue(1, 3, 3),
            SetValue(1, 8, 4),
            SetValue(2, 1, 7),
            SetValue(6, 7, 6),
            SetValue(7, 0, 8),
            SetValue(7, 8, 7),
            SetValue(8, 7, 4)
        )
        assertIterableEquals(expected, hiddenSingles(createCellBoardFromStringWithCandidates(board)).sorted())
    }
}