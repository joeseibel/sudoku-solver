package sudokusolver.kotlin

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SudokuSolverKtTest {
    @Test
    fun testSolveSolution() {
        val board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010"
        val expected = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        assertEquals(Solution(parseBoard(expected)), solve(parseOptionalBoard(board)))
    }

    @Test
    fun testSolveUnableToSolve() {
        val board = "004007830000050470720030695080700300649513728007008010470080060016040007005276100"
        val expected = """
            {159}{569}4{169}{269}783{12}
            {139}{36}{38}{689}5{129}47{12}
            72{18}{148}3{14}695
            {125}8{12}7{269}{249}3{45}{469}
            649513728
            {235}{35}7{46}{269}8{59}1{469}
            47{23}{139}8{159}{259}6{39}
            {2389}16{39}4{59}{25}{58}7
            {38}{39}52761{48}{349}
        """.trimIndent().replace("\n", "")
        assertEquals(UnableToSolve(parseCellsWithCandidates(expected)), solve(parseOptionalBoard(board)))
    }

    @Test
    fun testSolveInvalidNoSolutions() {
        val board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010"
        assertEquals(InvalidNoSolutions, solve(parseOptionalBoard(board)))
    }

    @Test
    fun testSolveInvalidMultipleSolutions() {
        val board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010"
        assertEquals(InvalidMultipleSolutions, solve(parseOptionalBoard(board)))
    }
}