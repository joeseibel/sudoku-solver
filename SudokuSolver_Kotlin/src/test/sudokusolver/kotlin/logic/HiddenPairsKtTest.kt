package sudokusolver.kotlin.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.assertRemoveCandidates
import sudokusolver.kotlin.createCellBoardFromStringWithCandidates

internal class HiddenPairsKtTest {
    /*
     * 0 0 0 | 0 0 0 | 0 0 0
     * 9 0 4 | 6 0 7 | 0 0 0
     * 0 7 6 | 8 0 4 | 1 0 0
     * ------+-------+------
     * 3 0 9 | 7 0 1 | 0 8 0
     * 7 0 8 | 0 0 0 | 3 0 1
     * 0 5 1 | 3 0 8 | 7 0 2
     * ------+-------+------
     * 0 0 7 | 5 0 2 | 6 1 0
     * 0 0 5 | 4 0 3 | 2 0 8
     * 0 0 0 | 0 0 0 | 0 0 0
     */
    @Test
    fun testHiddenPairs1() {
        val board = """
            {1258}{1238}{23}{129}{12359}{59}{4589}{2345679}{345679}
            9{1238}46{1235}7{58}{235}{35}
            {25}768{2359}41{2359}{359}
            3{246}97{2456}1{45}8{456}
            7{246}8{29}{24569}{569}3{4569}1
            {46}513{469}87{469}2
            {48}{3489}75{89}261{349}
            {16}{169}54{1679}32{79}8
            {12468}{1234689}{23}{19}{16789}{69}{459}{34579}{34579}
        """.trimIndent().replace("\n", "")
        val modifications = hiddenPairs(createCellBoardFromStringWithCandidates(board)).sorted()

        assertEquals(2, modifications.size)
        assertRemoveCandidates(modifications[0], 0, 7, 2, 3, 4, 5, 9)
        assertRemoveCandidates(modifications[1], 0, 8, 3, 4, 5, 9)
    }

    /*
     * 7 2 0 | 4 0 8 | 0 3 0
     * 0 8 0 | 0 0 0 | 0 4 7
     * 4 0 1 | 0 7 6 | 8 0 2
     * ------+-------+------
     * 8 1 0 | 7 3 9 | 0 0 0
     * 0 0 0 | 8 5 1 | 0 0 0
     * 0 0 0 | 2 6 4 | 0 8 0
     * ------+-------+------
     * 2 0 9 | 6 8 0 | 4 1 3
     * 3 4 0 | 0 0 0 | 0 0 8
     * 1 6 8 | 9 4 3 | 2 7 5
     */
    @Test
    fun testHiddenPairs2() {
        val board = """
            72{56}4{19}8{1569}3{169}
            {569}8{356}{135}{129}{25}{1569}47
            4{359}1{35}768{59}2
            81{2456}739{56}{256}{46}
            {69}{379}{23467}851{3679}{269}{469}
            {59}{3579}{357}264{13579}8{19}
            2{57}968{57}413
            34{57}{15}{12}{257}{69}{69}8
            168943275
        """.trimIndent().replace("\n", "")
        val modifications = hiddenPairs(createCellBoardFromStringWithCandidates(board)).sorted()

        assertEquals(4, modifications.size)
        assertRemoveCandidates(modifications[0], 3, 2, 5, 6)
        assertRemoveCandidates(modifications[1], 4, 2, 3, 6, 7)
        assertRemoveCandidates(modifications[2], 4, 6, 6, 9)
        assertRemoveCandidates(modifications[3], 5, 6, 1, 5, 9)
    }
}