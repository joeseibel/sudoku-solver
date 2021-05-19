package sudokusolver.kotlin.logic.simple

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class NakedQuadsKtTest {
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
        assertLogicalSolution(expected, board, ::nakedQuads)
    }
}