package sudokusolver.kotlin.logic.diabolical

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class WXYZWingKtTest {
    @Test
    fun test1() {
        val board = """
            {1689}{169}{189}{1589}2473{158}
            54{189}37{89}26{18}
            237{1568}{15}{568}{159}{189}4
            7{12569}{1259}{59}3{259}84{156}
            {69}{2569}3481{59}{279}{567}
            {19}84{579}6{2579}{159}{12}3
            3{12}{128}{1678}{14}{678}{46}59
            {148}7{158}{568}93{46}{18}2
            {1489}{159}62{145}{58}3{178}{178}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 1, 9),
            RemoveCandidates(7, 2, 1)
        )
        assertLogicalSolution(expected, board, ::wxyzWing)
    }

    @Test
    fun test2() {
        val board = """
            {1689}{169}{189}{1589}2473{158}
            54{189}37{89}26{18}
            237{1568}{15}{568}{159}{189}4
            7{1256}{1259}{59}3{259}84{156}
            {69}{2569}3481{59}{279}{567}
            {19}84{579}6{2579}{159}{12}3
            3{12}{128}{1678}{14}{678}{46}59
            {148}7{158}{1568}93{46}{18}2
            {1489}{159}62{145}{58}3{178}{178}
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(7, 2, 1))
        assertLogicalSolution(expected, board, ::wxyzWing)
    }

    @Test
    fun test3() {
        val board = """
            {18}6{89}{189}24735
            54{189}37{89}26{18}
            237{168}5{68}{19}{89}4
            7{15}{12}{59}3{29}846
            6{25}3481{59}{29}7
            984{57}6{27}{15}{12}3
            3{12}{28}{678}{14}{678}{46}59
            {148}75{68}93{46}{18}2
            {48}962{14}537{18}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 2, 1),
            RemoveCandidates(6, 4, 1),
            RemoveCandidates(8, 4, 4)
        )
        assertLogicalSolution(expected, board, ::wxyzWing)
    }

    @Test
    fun test4() {
        val board = """
            842{56}{56}3719
            {67}{679}31{789}4{568}{258}{26}
            5{679}1{27}{2789}{289}{68}34
            {69}38{26}1{269}475
            {49}2{45}3{59}7168
            1{56}7{456}{4568}{568}293
            3{58}6{2457}{2457}{25}9{458}1
            {47}{578}{45}931{568}{258}{26}
            2198{456}{56}3{45}7
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(5, 4, 6),
            RemoveCandidates(5, 5, 5),
            RemoveCandidates(6, 4, 5),
            RemoveCandidates(8, 4, 5)
        )
        assertLogicalSolution(expected, board, ::wxyzWing)
    }

    @Test
    fun test5() {
        val board = """
            842{56}{56}3719
            {67}{679}31{789}4{568}{258}{26}
            5{679}1{27}{2789}{289}{68}34
            {69}38{26}1{269}475
            {49}2{45}3{59}7168
            1{56}7{456}{4568}{68}293
            3{58}6{247}{247}{25}9{458}1
            {47}{578}{45}931{568}{258}{26}
            2198{46}{56}3{45}7
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(5, 4, 6))
        assertLogicalSolution(expected, board, ::wxyzWing)
    }

    @Test
    fun test6() {
        val board = """
            {2457}{135}{1257}{3467}{2367}{247}8{3456}9
            {2479}{39}8{34679}{23679}5{13}{346}{136}
            {459}6{59}1{389}{489}7{345}2
            {589}{589}{2569}{679}{15679}3{1259}{279}4
            {259}7{23569}{69}4{19}{12359}8{135}
            14{359}2{5789}{789}6{379}{357}
            3{589}4{789}{279}6{259}1{578}
            {789}2{179}5{1379}{179}4{3679}{3678}
            6{1589}{1579}{34789}{12379}{12479}{2359}{2379}{3578}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 6, 3),
            RemoveCandidates(1, 7, 3),
            RemoveCandidates(1, 8, 3)
        )
        assertLogicalSolution(expected, board, ::wxyzWing)
    }

    @Test
    fun test7() {
        val board = """
            96{48}{258}{28}137{45}
            {134}{14}2{35}9786{45}
            {38}5764{38}{12}{129}{19}
            {18}2{1368}{3489}5{3489}7{189}{169}
            7{489}{348}1{38}65{89}2
            5{19}{168}{289}7{289}43{1689}
            {1248}7{14}{2348}6{2348}95{138}
            {24}35{2489}1{2489}6{28}7
            6{18}97{238}5{12}4{13}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 3, 8),
            RemoveCandidates(3, 5, 8),
            RemoveCandidates(4, 1, 8),
            RemoveCandidates(5, 2, 8)
        )
        assertLogicalSolution(expected, board, ::wxyzWing)
    }

    @Test
    fun test8() {
        val board = """
            96{48}{258}{28}137{45}
            {134}{14}2{35}9786{45}
            {38}5764{38}{12}{129}{19}
            {18}2{1368}{349}5{349}7{189}{169}
            7{489}{348}1{38}65{89}2
            5{19}{16}{289}7{289}43{1689}
            {1248}7{14}{2348}6{2348}95{138}
            {24}35{2489}1{2489}6{28}7
            6{18}97{238}5{12}4{13}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 7, 9),
            RemoveCandidates(3, 8, 9),
            RemoveCandidates(4, 1, 8)
        )
        assertLogicalSolution(expected, board, ::wxyzWing)
    }
}