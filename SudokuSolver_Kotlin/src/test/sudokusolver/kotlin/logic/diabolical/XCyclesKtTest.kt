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
    fun testXCyclesRule2() {
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

    /*
     * 0 7 6 | 2 0 0 | 4 0 0
     * 0 9 4 | 1 0 7 | 0 6 0
     * 2 0 0 | 4 6 0 | 0 0 7
     * ------+-------+------
     * 0 6 0 | 3 7 1 | 0 0 0
     * 7 4 0 | 5 9 2 | 0 1 6
     * 0 0 0 | 6 8 4 | 0 7 0
     * ------+-------+------
     * 3 0 9 | 7 0 6 | 0 0 5
     * 6 8 0 | 9 0 5 | 7 3 0
     * 4 5 7 | 8 0 3 | 6 0 0
     */
    @Test
    fun testXCyclesRule3Test1() {
        val board = """
            {158}762{35}{89}4{589}{1389}
            {58}941{35}7{2358}6{238}
            2{13}{1358}46{89}{1589}{589}7
            {589}6{258}371{2589}{24589}{2489}
            74{38}592{38}16
            {159}{123}{1235}684{2359}7{239}
            3{12}97{124}6{128}{248}5
            68{12}9{124}573{124}
            4578{12}36{29}{129}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 2, 1)
        )
        assertIterableEquals(expected, xCyclesRule3(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 0 0 0 | 0 1 0 | 9 6 0
     * 0 0 0 | 6 8 0 | 4 5 0
     * 0 5 6 | 9 4 2 | 3 0 7
     * ------+-------+------
     * 0 0 0 | 0 0 8 | 0 0 9
     * 3 8 0 | 0 9 4 | 6 2 5
     * 9 0 0 | 2 0 0 | 0 0 4
     * ------+-------+------
     * 6 7 3 | 0 2 9 | 5 4 0
     * 5 0 8 | 4 7 6 | 0 0 3
     * 0 4 0 | 0 5 0 | 0 0 6
     */
    @Test
    fun testXCyclesRule3Test2() {
        val board = """
            {2478}{23}{247}{357}1{357}96{28}
            {127}{1239}{1279}68{37}45{12}
            {18}569423{18}7
            {1247}{126}{12457}{157}{36}8{17}{137}9
            38{17}{17}94625
            9{16}{157}2{36}{157}{178}{1378}4
            673{18}2954{18}
            5{129}8476{12}{19}3
            {12}4{129}{138}5{13}{1278}{1789}6
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(8, 2, 1)
        )
        assertIterableEquals(expected, xCyclesRule3(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 0 0 0 | 9 7 8 | 0 0 0
     * 0 8 3 | 0 0 1 | 5 9 0
     * 0 9 0 | 2 5 3 | 0 1 0
     * ------+-------+------
     * 0 7 4 | 5 8 6 | 0 0 0
     * 8 6 0 | 1 3 4 | 0 5 0
     * 0 3 0 | 7 9 2 | 6 8 4
     * ------+-------+------
     * 3 2 0 | 8 0 9 | 0 7 0
     * 0 0 8 | 0 0 0 | 3 0 0
     * 0 0 0 | 3 2 0 | 0 0 0
     */
    @Test
    fun testXCyclesRule3Test3() {
        val board = """
            {12456}{145}{1256}978{24}{2346}{236}
            {27}83{46}{46}159{27}
            {467}9{67}253{478}1{678}
            {29}74586{129}{23}{1239}
            86{29}134{279}5{279}
            {15}3{15}792684
            32{156}8{146}9{14}7{156}
            {145679}{145}8{46}{146}{57}3{246}{12569}
            {145679}{145}{15679}32{57}{1489}{46}{15689}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(4, 8, 2, 9)
        )
        assertIterableEquals(expected, xCyclesRule3(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 0 0 0 | 0 5 0 | 0 0 0
     * 0 0 1 | 2 0 3 | 9 0 5
     * 0 5 0 | 0 0 7 | 0 0 0
     * ------+-------+------
     * 9 7 3 | 4 2 1 | 6 5 8
     * 1 6 5 | 7 3 8 | 4 2 9
     * 4 0 0 | 9 6 5 | 0 0 1
     * ------+-------+------
     * 0 0 0 | 5 7 0 | 0 8 0
     * 0 0 7 | 3 0 4 | 5 0 0
     * 5 0 0 | 0 1 0 | 0 0 0
     */
    @Test
    fun testXCyclesRule3Test4() {
        val board = """
            {23678}{23489}{24689}{168}5{69}{12378}{13467}{23467}
            {67}{48}12{48}39{67}5
            {2368}5{24689}{168}{489}7{1238}{1346}{2346}
            973421658
            165738429
            4{28}{28}965{37}{37}1
            {236}{12349}{2469}57{269}{123}8{2346}
            {268}{1289}73{89}45{169}{26}
            5{23489}{24689}{68}1{269}{237}{34679}{23467}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(8, 1, 8),
            RemoveCandidates(8, 2, 8)
        )
        assertIterableEquals(expected, xCyclesRule3(parseCellsWithCandidates(board)).sorted())
    }
}