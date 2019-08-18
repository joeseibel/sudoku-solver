package sudokusolver.kotlin.logic.diabolical

import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.parseCellsWithCandidates

internal class XCyclesKtTest {
    @Test
    fun testToDOT() {
        val a = UnsolvedCell(0, 0)
        val b = UnsolvedCell(0, 3)
        val c = UnsolvedCell(2, 5)
        val dot = GraphBuilder(SimpleGraph<UnsolvedCell, XCyclesEdge>(XCyclesEdge::class.java))
            .addEdge(a, b, XCyclesEdge(true))
            .addEdge(b, c, XCyclesEdge(false))
            .buildAsUnmodifiable()
            .toDOT(SudokuNumber.ONE)
        assertEquals("""
            strict graph 1 {
              1 [ label="[0,0]" ];
              2 [ label="[0,3]" ];
              3 [ label="[2,5]" ];
              1 -- 2;
              2 -- 3 [ style="dashed" ];
            }
            
        """.trimIndent(), dot)
    }

    /*
     * 0 2 4 | 1 0 0 | 6 7 0
     * 0 6 0 | 0 7 0 | 4 1 0
     * 7 0 0 | 9 6 4 | 0 2 0
     * ------+-------+------
     * 2 4 6 | 5 9 1 | 3 8 7
     * 1 3 5 | 4 8 7 | 2 9 6
     * 8 7 9 | 6 2 3 | 1 5 4
     * ------+-------+------
     * 4 0 0 | 0 0 9 | 7 6 0
     * 3 5 0 | 7 1 6 | 9 4 0
     * 6 9 7 | 0 4 0 | 0 3 1
     */
    @Test
    fun testXCyclesRule1() {
        val board = """
            {59}241{35}{58}67{389}
            {59}6{38}{238}7{258}41{389}
            7{18}{138}964{58}2{358}
            246591387
            135487296
            879623154
            4{18}{128}{38}{35}976{258}
            35{28}71694{28}
            697{28}4{258}{58}31
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 2, 8),
            RemoveCandidates(2, 8, 8),
            RemoveCandidates(6, 2, 8),
            RemoveCandidates(6, 8, 8)
        )
        assertIterableEquals(expected, xCyclesRule1(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 8 0 4 | 5 3 7 | 0 0 0
     * 0 2 3 | 6 1 4 | 0 8 5
     * 6 0 5 | 9 8 2 | 0 3 4
     * ------+-------+------
     * 0 0 0 | 1 0 5 | 8 7 0
     * 5 0 0 | 7 0 8 | 3 0 6
     * 0 8 0 | 2 0 3 | 4 5 0
     * ------+-------+------
     * 2 0 0 | 8 5 9 | 0 0 3
     * 0 5 0 | 3 7 1 | 2 0 8
     * 0 0 8 | 4 2 6 | 5 0 7
     */
    @Test
    fun textXCyclesRule2() {
        val board = """
            8{19}4537{169}{126}{12}
            {79}23614{79}85
            6{17}5982{17}34
            {349}{346}{269}1{469}587{29}
            5{49}{12}7{49}83{12}6
            {179}8{1679}2{69}345{19}
            2{467}{167}859{16}{146}3
            {49}5{69}3712{469}8
            {139}{39}84265{19}7
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            SetValue(8, 0, 1)
        )
        assertIterableEquals(expected, xCyclesRule2(parseCellsWithCandidates(board)).sorted())
    }
}