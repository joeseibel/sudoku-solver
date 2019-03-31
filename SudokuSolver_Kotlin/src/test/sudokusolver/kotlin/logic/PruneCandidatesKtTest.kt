package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.createCellBoardFromSimpleString

internal class PruneCandidatesKtTest {
    /*
     * 0 0 0 | 1 0 5 | 0 0 0
     * 1 4 0 | 0 0 0 | 6 7 0
     * 0 8 0 | 0 0 2 | 4 0 0
     * ------+-------+------
     * 0 6 3 | 0 7 0 | 0 1 0
     * 9 0 0 | 0 0 0 | 0 0 3
     * 0 1 0 | 0 9 0 | 5 2 0
     * ------+-------+------
     * 0 0 7 | 2 0 0 | 0 8 0
     * 0 2 6 | 0 0 0 | 0 3 5
     * 0 0 0 | 4 0 9 | 0 0 0
     */
    @Test
    fun testPruneCandidates() {
        val board = "000105000140000670080002400063070010900000003010090520007200080026000035000409000"
        val expected = listOf(
            RemoveCandidates(0, 0, 1, 4, 5, 8, 9),
            RemoveCandidates(0, 1, 1, 2, 4, 5, 6, 8),
            RemoveCandidates(0, 2, 1, 3, 4, 5, 6, 7, 8),
            RemoveCandidates(0, 4, 1, 2, 5, 7, 9),
            RemoveCandidates(0, 6, 1, 4, 5, 6, 7),
            RemoveCandidates(0, 7, 1, 2, 3, 4, 5, 6, 7, 8),
            RemoveCandidates(0, 8, 1, 3, 4, 5, 6, 7),
            RemoveCandidates(1, 2, 1, 3, 4, 6, 7, 8),
            RemoveCandidates(1, 3, 1, 2, 4, 5, 6, 7),
            RemoveCandidates(1, 4, 1, 2, 4, 5, 6, 7, 9),
            RemoveCandidates(1, 5, 1, 2, 4, 5, 6, 7, 9),
            RemoveCandidates(1, 8, 1, 3, 4, 5, 6, 7),
            RemoveCandidates(2, 0, 1, 2, 4, 8, 9),
            RemoveCandidates(2, 2, 1, 2, 3, 4, 6, 7, 8),
            RemoveCandidates(2, 3, 1, 2, 4, 5, 8),
            RemoveCandidates(2, 4, 1, 2, 4, 5, 7, 8, 9),
            RemoveCandidates(2, 7, 1, 2, 3, 4, 6, 7, 8),
            RemoveCandidates(2, 8, 2, 3, 4, 5, 6, 7, 8),
            RemoveCandidates(3, 0, 1, 3, 6, 7, 9),
            RemoveCandidates(3, 3, 1, 2, 3, 4, 6, 7, 9),
            RemoveCandidates(3, 5, 1, 2, 3, 5, 6, 7, 9),
            RemoveCandidates(3, 6, 1, 2, 3, 4, 5, 6, 7),
            RemoveCandidates(3, 8, 1, 2, 3, 5, 6, 7),
            RemoveCandidates(4, 1, 1, 2, 3, 4, 6, 8, 9),
            RemoveCandidates(4, 2, 1, 3, 6, 7, 9),
            RemoveCandidates(4, 3, 1, 2, 3, 4, 7, 9),
            RemoveCandidates(4, 4, 3, 7, 9),
            RemoveCandidates(4, 5, 2, 3, 5, 7, 9),
            RemoveCandidates(4, 6, 1, 2, 3, 4, 5, 6, 9),
            RemoveCandidates(4, 7, 1, 2, 3, 5, 7, 8, 9),
            RemoveCandidates(5, 0, 1, 2, 3, 5, 6, 9),
            RemoveCandidates(5, 2, 1, 2, 3, 5, 6, 7, 9),
            RemoveCandidates(5, 3, 1, 2, 4, 5, 7, 9),
            RemoveCandidates(5, 5, 1, 2, 5, 7, 9),
            RemoveCandidates(5, 8, 1, 2, 3, 5, 9),
            RemoveCandidates(6, 0, 1, 2, 6, 7, 8, 9),
            RemoveCandidates(6, 1, 1, 2, 4, 6, 7, 8),
            RemoveCandidates(6, 4, 2, 4, 7, 8, 9),
            RemoveCandidates(6, 5, 2, 4, 5, 7, 8, 9),
            RemoveCandidates(6, 6, 2, 3, 4, 5, 6, 7, 8),
            RemoveCandidates(6, 8, 2, 3, 5, 7, 8),
            RemoveCandidates(7, 0, 1, 2, 3, 5, 6, 7, 9),
            RemoveCandidates(7, 3, 1, 2, 3, 4, 5, 6, 9),
            RemoveCandidates(7, 4, 2, 3, 4, 5, 6, 7, 9),
            RemoveCandidates(7, 5, 2, 3, 4, 5, 6, 9),
            RemoveCandidates(7, 6, 2, 3, 4, 5, 6, 8),
            RemoveCandidates(8, 0, 1, 2, 4, 6, 7, 9),
            RemoveCandidates(8, 1, 1, 2, 4, 6, 7, 8, 9),
            RemoveCandidates(8, 2, 2, 3, 4, 6, 7, 9),
            RemoveCandidates(8, 4, 2, 4, 7, 9),
            RemoveCandidates(8, 6, 3, 4, 5, 6, 8, 9),
            RemoveCandidates(8, 7, 1, 2, 3, 4, 5, 7, 8, 9),
            RemoveCandidates(8, 8, 3, 4, 5, 8, 9)
        )
        assertIterableEquals(expected, pruneCandidates(createCellBoardFromSimpleString(board)).sorted())
    }
}