package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.assertRemoveCandidates
import sudokusolver.kotlin.createCellBoardFromStringWithCandidates

internal class HiddenQuadsKtTest {
    /*
     * 6 5 0 | 0 8 7 | 0 2 4
     * 0 0 0 | 6 4 9 | 0 5 0
     * 0 4 0 | 0 2 5 | 0 0 0
     * ------+-------+------
     * 5 7 0 | 4 3 8 | 0 6 1
     * 0 0 0 | 5 0 1 | 0 0 0
     * 3 1 0 | 9 0 2 | 0 8 5
     * ------+-------+------
     * 0 0 0 | 8 9 0 | 0 1 0
     * 0 0 0 | 2 1 3 | 0 0 0
     * 1 3 0 | 7 5 0 | 0 9 8
     */
    @Test
    fun testHiddenQuads1() {
        val board = """
            65{139}{13}87{19}24
            {278}{28}{1378}649{18}5{37}
            {89}4{378}{13}25{168}{37}{69}
            57{29}438{29}61
            {2489}{2689}{468}5{67}1{347}{347}{29}
            31{46}9{67}2{47}85
            {247}{26}{457}89{46}{357}1{237}
            {4789}{689}{578}213{4576}{47}{67}
            13{246}75{46}{26}98
        """.trimIndent().replace("\n", "")
        val modifications = hiddenQuads(createCellBoardFromStringWithCandidates(board))

        assertEquals(1, modifications.size)
        assertRemoveCandidates(modifications[0], 7, 6, 6)
    }

    /*
     * 9 0 1 | 5 0 0 | 0 4 6
     * 4 2 5 | 0 9 0 | 0 8 1
     * 8 6 0 | 0 1 0 | 0 2 0
     * ------+-------+------
     * 5 0 2 | 0 0 0 | 0 0 0
     * 0 1 9 | 0 0 0 | 4 6 0
     * 6 0 0 | 0 0 0 | 0 0 2
     * ------+-------+------
     * 1 9 6 | 0 4 0 | 2 5 3
     * 2 0 0 | 0 6 0 | 8 1 7
     * 0 0 0 | 0 0 1 | 6 9 4
     */
    @Test
    fun testHiddenQuads2() {
        val board = """
            9{37}15{28}{28}{37}46
            425{367}9{367}{37}81
            86{37}{347}1{347}{59}2{59}
            5{3478}2{1346789}{378}{346789}{19}{37}{89}
            {37}19{2378}{23578}{23578}46{58}
            6{3478}{3478}{134789}{3578}{345789}{159}{37}2
            196{78}4{78}253
            2{345}{34}{39}6{359}817
            {37}{3578}{378}{23}{235}1694
        """.trimIndent().replace("\n", "")
        val modifications = hiddenQuads(createCellBoardFromStringWithCandidates(board))

        assertEquals(4, modifications.size)
        assertRemoveCandidates(modifications[0], 3, 3, 3, 7, 8)
        assertRemoveCandidates(modifications[1], 3, 5, 3, 7, 8)
        assertRemoveCandidates(modifications[2], 5, 3, 3, 7, 8)
        assertRemoveCandidates(modifications[3], 5, 5, 3, 5, 7, 8)
    }
}