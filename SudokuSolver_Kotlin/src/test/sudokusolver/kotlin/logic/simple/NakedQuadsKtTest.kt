package sudokusolver.kotlin.logic.simple

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.parseCellsWithCandidates

internal class NakedQuadsKtTest {
    /*
     * 0 0 0 | 0 3 0 | 0 8 6
     * 0 0 0 | 0 2 0 | 0 4 0
     * 0 9 0 | 0 7 8 | 5 2 0
     * ------+-------+------
     * 3 7 1 | 8 5 6 | 2 9 4
     * 9 0 0 | 1 4 2 | 3 7 5
     * 4 0 0 | 3 9 7 | 6 1 8
     * ------+-------+------
     * 2 0 0 | 7 0 3 | 8 5 9
     * 0 3 9 | 2 0 5 | 4 6 7
     * 7 0 0 | 9 0 4 | 1 3 2
     */
    @Test
    fun testNakedQuads() {
        val board = """
            {15}{1245}{2457}{45}3{19}{79}86
            {1568}{1568}{35678}{56}2{19}{79}4{13}
            {16}9{346}{46}7852{13}
            371856294
            9{68}{68}142375
            4{25}{25}397618
            2{146}{46}7{16}3859
            {18}392{18}5467
            7{568}{568}9{68}4132
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 1, 1, 5),
            RemoveCandidates(0, 2, 5),
            RemoveCandidates(1, 2, 5, 6, 8),
            RemoveCandidates(2, 2, 6)
        )
        assertIterableEquals(expected, nakedQuads(parseCellsWithCandidates(board)).sorted())
    }
}