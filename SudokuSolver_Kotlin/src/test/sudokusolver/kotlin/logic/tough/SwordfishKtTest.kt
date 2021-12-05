package sudokusolver.kotlin.logic.tough

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class SwordfishKtTest {
    @Test
    fun test1() {
        val board = """
            52941{68}7{68}3
            {478}{148}6{59}{789}3{18}{14589}2
            {478}{148}32{789}{56}{189}{56}{1489}
            {48}523{89}{148}{189}76
            637{19}5{148}2{1489}{1489}
            19{48}62753{48}
            3{78}{158}{15}6942{178}
            2{47}{145}83{15}6{19}{179}
            96{18}7423{18}5
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 1, 8),
            RemoveCandidates(1, 7, 8),
            RemoveCandidates(2, 1, 8),
            RemoveCandidates(2, 8, 8),
            RemoveCandidates(3, 5, 8)
        )
        assertLogicalSolution(expected, board, ::swordfish)
    }

    @Test
    fun test2() {
        val board = """
            926{3458}{48}{3578}1{57}{578}
            537{689}1{689}42{89}
            841{259}{59}{2579}6{579}3
            259734816
            714{589}6{589}{259}3{259}
            36812{59}{579}4{579}
            1{79}2{36}{59}{36}{579}84
            485{29}7136{29}
            6{79}3{24589}{48}{2589}{2579}{579}1
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 3, 9),
            RemoveCandidates(2, 5, 9),
            RemoveCandidates(6, 6, 9),
            RemoveCandidates(8, 3, 9),
            RemoveCandidates(8, 5, 9),
            RemoveCandidates(8, 6, 9)
        )
        assertLogicalSolution(expected, board, ::swordfish)
    }

    @Test
    fun test3() {
        val board = """
            {157}2{1578}{17}43{1578}69
            {1457}{145}38962{45}{1457}
            96{1478}{17}25{1478}3{1478}
            89{247}56{27}{47}13
            6{145}{12457}{249}3{279}{45789}{458}{4578}
            {457}3{457}{49}81{4579}26
            3{458}{456}{29}1{29}{4568}7{458}
            {15}{158}96743{58}2
            27{46}358{146}9{14}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 1, 4),
            RemoveCandidates(1, 8, 4),
            RemoveCandidates(4, 1, 4),
            RemoveCandidates(4, 2, 4),
            RemoveCandidates(4, 6, 4),
            RemoveCandidates(4, 8, 4),
            RemoveCandidates(5, 2, 4),
            RemoveCandidates(5, 6, 4),
            RemoveCandidates(6, 2, 4),
            RemoveCandidates(6, 6, 4),
            RemoveCandidates(6, 8, 4)
        )
        assertLogicalSolution(expected, board, ::swordfish)
    }

    @Test
    fun test4() {
        val board = """
            1673{259}{259}{289}4{589}
            8{23}{239}{245}{12459}6{1279}{2579}{159}
            {29}5487{129}63{19}
            {236}9{2368}7{2348}{238}51{346}
            {2356}{234}{123568}{245}{1234589}{123589}{239}{269}7
            7{234}{1235}{245}6{12359}{239}8{349}
            {2356}7{2356}9{2358}4{138}{56}{13568}
            {3569}8{3569}1{35}{357}4{5679}2
            41{2359}6{2358}{23578}{3789}{579}{3589}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 2, 2),
            RemoveCandidates(1, 4, 2),
            RemoveCandidates(1, 6, 2),
            RemoveCandidates(4, 0, 2),
            RemoveCandidates(4, 2, 2),
            RemoveCandidates(4, 4, 2),
            RemoveCandidates(4, 5, 2),
            RemoveCandidates(4, 6, 2),
            RemoveCandidates(5, 2, 2),
            RemoveCandidates(5, 5, 2),
            RemoveCandidates(5, 6, 2)
        )
        assertLogicalSolution(expected, board, ::swordfish)
    }

    @Test
    fun test5() {
        val board = """
            3{26789}{12678}{1689}4{1269}{126}5{16}
            {12}{269}{1256}{1569}37{126}48
            {128}{2468}{124568}{1568}{168}{126}937
            {27}1{267}4{679}358{69}
            4{3678}{3678}{1679}5{169}{16}2{1369}
            95{36}2{16}847{136}
            5{478}{1478}3{1789}{149}{78}62
            {1278}{23478}{123478}{1678}{1678}{146}{78}95
            6{78}9{78}25314
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 1, 8),
            RemoveCandidates(2, 2, 8),
            RemoveCandidates(2, 3, 8),
            RemoveCandidates(3, 2, 7),
            RemoveCandidates(6, 1, 7, 8),
            RemoveCandidates(6, 2, 7, 8),
            RemoveCandidates(7, 1, 7, 8),
            RemoveCandidates(7, 2, 7, 8),
            RemoveCandidates(7, 3, 7, 8)
        )
        assertLogicalSolution(expected, board, ::swordfish)
    }
}