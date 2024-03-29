package sudokusolver.kotlin.logic.simple

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class HiddenQuadsKtTest {
    @Test
    fun test1() {
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
        val expected = listOf(RemoveCandidates(7, 6, 6))
        assertLogicalSolution(expected, board, ::hiddenQuads)
    }

    @Test
    fun test2() {
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
        val expected = listOf(
            RemoveCandidates(3, 3, 3, 7, 8),
            RemoveCandidates(3, 5, 3, 7, 8),
            RemoveCandidates(5, 3, 3, 7, 8),
            RemoveCandidates(5, 5, 3, 5, 7, 8)
        )
        assertLogicalSolution(expected, board, ::hiddenQuads)
    }
}