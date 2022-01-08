package sudokusolver.kotlin.logic.extreme

import org.jgrapht.graph.SimpleGraph
import org.jgrapht.graph.builder.GraphBuilder
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.Strength
import sudokusolver.kotlin.StrengthEdge
import sudokusolver.kotlin.SudokuNumber
import sudokusolver.kotlin.UnsolvedCell
import sudokusolver.kotlin.logic.assertLogicalSolution
import kotlin.test.assertEquals

internal class GroupedXCyclesKtTest {
    @Test
    fun testToDOT() {
        val a = CellNode(UnsolvedCell(6, 1))
        val b = RowGroup(
            setOf(UnsolvedCell(6, 6), UnsolvedCell(6, 7), UnsolvedCell(6, 8))
        )
        val c = ColumnGroup(setOf(UnsolvedCell(6, 2), UnsolvedCell(8, 2)))
        val actual = GraphBuilder(SimpleGraph<Node, StrengthEdge>(StrengthEdge::class.java))
            .addEdge(a, b, StrengthEdge(Strength.WEAK))
            .addEdge(a, c, StrengthEdge(Strength.STRONG))
            .buildAsUnmodifiable()
            .toDOT(SudokuNumber.EIGHT)
        val expected = """
            strict graph 8 {
              1 [ label="[6,1]" ];
              2 [ label="{[6,6], [6,7], [6,8]}" ];
              3 [ label="{[6,2], [8,2]}" ];
              1 -- 2 [ style="dashed" ];
              1 -- 3;
            }
            
        """.trimIndent()
        assertEquals(expected, actual)
    }

    @Test
    fun rule1Test1() {
        val board = """
            185{49}2637{49}
            {234}6{234}{3579}{134}{1357}{2458}{28}{2589}
            {234}97{345}{34}81{26}{2456}
            {4678}1{48}{348}52{68}9{37}
            {245789}{27}{2489}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {23789}{27}{2389}{357}{1348}{13457}{2468}{12368}{123468}
            {38}5629{134}7{138}{1348}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 3, 4),
            RemoveCandidates(2, 8, 4),
            RemoveCandidates(7, 5, 4),
            RemoveCandidates(7, 8, 4)
        )
        assertLogicalSolution(expected, board, ::groupedXCyclesRule1)
    }

    @Test
    fun rule1Test2() {
        val board = """
            3{279}1{89}{258}4{259}6{257}
            8{279}4{69}{256}{59}{2359}1{2357}
            56{29}713{289}{289}4
            {147}3{578}{46}{56}2{16}{78}9
            {147}{128}{2578}{346}9{578}{16}{2378}{2358}
            6{289}{25789}1{3578}{578}{2358}4{2358}
            {17}{18}{378}546{2389}{2389}{238}
            256{389}{378}{789}4{38}1
            94{38}2{38}1756
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 7, 8),
            RemoveCandidates(4, 2, 8),
            RemoveCandidates(4, 7, 8),
            RemoveCandidates(5, 2, 8),
            RemoveCandidates(5, 6, 8),
            RemoveCandidates(6, 2, 8),
            RemoveCandidates(6, 6, 8),
            RemoveCandidates(6, 7, 8)
        )
        assertLogicalSolution(expected, board, ::groupedXCyclesRule1)
    }

    @Test
    fun rule1Test3() {
        val board = """
            3{279}1{89}{258}4{259}6{257}
            8{279}4{69}{256}{59}{2359}1{2357}
            56{29}7138{29}4
            {147}3{578}{46}{56}2{16}{78}9
            {147}{128}{2578}{346}9{578}{16}{2378}{2358}
            6{289}{25789}1{3578}{578}{235}4{2358}
            {17}{18}{378}546{239}{2389}{238}
            256{389}{378}{789}4{38}1
            94{38}2{38}1756
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(4, 2, 8),
            RemoveCandidates(4, 7, 8),
            RemoveCandidates(5, 2, 8),
            RemoveCandidates(6, 2, 8),
            RemoveCandidates(6, 7, 8)
        )
        assertLogicalSolution(expected, board, ::groupedXCyclesRule1)
    }

    @Test
    fun rule2Test1() {
        val board = """
            {123}8{249}5{12}7{234}6{12349}
            7{36}{25}94{126}{235}{1235}8
            {125}{2456}{2459}38{26}7{259}{1249}
            {56}7{456}{246}981{23}{235}
            {26}18{26}53947
            9{245}3{124}7{12}68{25}
            8{23}1765{234}{239}{2349}
            4{25}7{12}398{125}6
            {356}9{26}8{12}4{235}7{123}
        """.trimIndent().replace("\n", "")
        val expected = listOf(SetValue(4, 0, 2))
        assertLogicalSolution(expected, board, ::groupedXCyclesRule2)
    }

    @Test
    fun rule2Test2() {
        val board = """
            2{168}4{16}{36}79{38}5
            {168}9{178}5{136}2{167}4{38}
            {167}354982{167}{16}
            {138}{178}6{78}295{13}4
            {135}{1578}2{78}4{16}{67}9{136}
            {17}4935{16}8{167}2
            {1568}{156}3974{16}2{168}
            92{18}{16}{168}3457
            4{1678}{178}2{18}53{168}9
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            SetValue(0, 7, 8),
            SetValue(6, 8, 8)
        )
        assertLogicalSolution(expected, board, ::groupedXCyclesRule2)
    }

    @Test
    fun rule3Test1() {
        val board = """
            {128}{124}37{89}65{2489}{49}
            7{248}{48}5{2389}{38}6{2489}1
            569{128}4{18}{38}7{238}
            {1368}{148}2{489}{137}{348}{38}5{3679}
            {38}956{378}241{378}
            {1368}7{48}{489}{138}52{689}{3689}
            9{28}6{14}5{14}73{28}
            437{28}{268}91{68}5
            {28}513{68}79{2468}{2468}
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(2, 3, 8))
        assertLogicalSolution(expected, board, ::groupedXCyclesRule3)
    }

    @Test
    fun rule3Test2() {
        val board = """
            185{49}2637{49}
            {234}6{234}{3579}{134}{1357}{2458}{28}{59}
            {234}97{35}{34}81{26}{256}
            {4678}1{48}{38}52{68}9{37}
            {2579}{27}{29}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {3789}{27}{2389}{357}{1348}{1357}{2468}{12368}{12368}
            {38}5629{134}7{138}{1348}
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(1, 6, 2))
        assertLogicalSolution(expected, board, ::groupedXCyclesRule3)
    }

    @Test
    fun rule3Test3() {
        val board = """
            185{49}2637{49}
            {234}6{23}{3579}{134}{1357}{458}{28}{59}
            {234}97{35}{34}81{26}{256}
            {678}14{38}52{68}9{37}
            {2579}{27}{29}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {3789}{27}{2389}{357}{1348}{1357}{2468}{12368}{12368}
            {38}5629{134}7{138}{1348}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(7, 6, 8),
            RemoveCandidates(7, 7, 8)
        )
        assertLogicalSolution(expected, board, ::groupedXCyclesRule3)
    }

    @Test
    fun rule3Test4() {
        val board = """
            185{49}2637{49}
            {234}6{23}{3579}{134}{1357}{458}{28}{59}
            {234}97{35}{34}81{26}{256}
            {678}14{38}52{68}9{37}
            {2579}{27}{29}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {3789}{27}{2389}{357}{1348}{1357}{246}{12368}{12368}
            {38}5629{134}7{138}{1348}
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(7, 7, 8))
        assertLogicalSolution(expected, board, ::groupedXCyclesRule3)
    }

    @Test
    fun rule3Test5() {
        val board = """
            1{278}5{37}{238}946{278}
            3496{28}{278}{2578}1{2578}
            {268}{278}{268}1453{289}{2789}
            {248}9{248}{58}1{468}{2568}73
            56{238}{37}9{78}14{28}
            71{348}2{3568}{46}{568}{589}{5689}
            {2468}5{2468}971{268}3{2468}
            {2468}{28}7{458}{2568}39{258}1
            931{458}{2568}{268}{25678}{258}{245678}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 0, 2),
            RemoveCandidates(2, 2, 2),
            RemoveCandidates(7, 1, 2)
        )
        assertLogicalSolution(expected, board, ::groupedXCyclesRule3)
    }

    @Test
    fun rule3Test6() {
        val board = """
            62{489}{48}53{489}71
            31{489}{478}{24789}{249}{2489}{56}{56}
            {45}{4589}71{2489}63{249}{48}
            {2457}{4589}{2489}3{489}16{2459}{4578}
            {247}{3489}{23489}{4568}{4689}{4589}{24789}1{3478}
            1{34589}62{489}7{489}{459}{3458}
            {24}{34}19{23478}{248}5{46}{467}
            87{245}{56}{1246}{245}{14}39
            96{345}{457}{1347}{45}{147}82
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 4, 4),
            RemoveCandidates(3, 2, 4),
            RemoveCandidates(4, 2, 4),
            RemoveCandidates(4, 6, 4),
            RemoveCandidates(5, 6, 4),
            RemoveCandidates(6, 4, 4),
            RemoveCandidates(6, 5, 4)
        )
        assertLogicalSolution(expected, board, ::groupedXCyclesRule3)
    }
}