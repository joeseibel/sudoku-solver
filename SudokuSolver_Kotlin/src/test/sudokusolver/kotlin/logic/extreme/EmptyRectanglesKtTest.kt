package sudokusolver.kotlin.logic.extreme

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class EmptyRectanglesKtTest {
    @Test
    fun test1() {
        val board = """
            4{256}{567}1{269}3{67}{678}{789}
            {36}8{13}5{69}742{1369}
            9{126}{37}{68}4{268}{136}{367}5
            139{468}{26}{248}5{678}{678}
            27{56}91{568}{36}4{368}
            8{56}473{56}912
            592{46}8{146}{1367}{367}{137}
            74835{16}29{16}
            {36}{16}{136}279854
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(3, 8, 6))
        assertLogicalSolution(expected, board, ::emptyRectangles)
    }

    @Test
    fun test2() {
        val board = """
            75{148}96{148}32{18}
            {3468}{36}{13469}7{48}2{689}5{1689}
            {68}{26}{12689}{158}3{158}{689}47
            97{246}{126}5{14}{246}83
            {346}{236}5{2368}7{389}1{69}{2469}
            18{236}{236}{24}{349}{2469}75
            24{68}{35}9{35}71{68}
            {3568}1{368}4{28}7{2569}{69}{2689}
            {58}97{28}16{2458}3{248}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(4, 8, 6),
            RemoveCandidates(7, 2, 6)
        )
        assertLogicalSolution(expected, board, ::emptyRectangles)
    }

    @Test
    fun test3() {
        val board = """
            9{37}15{28}{28}{37}46
            425{367}9{367}{37}81
            86{37}{347}1{347}{59}2{59}
            5{3478}2{1469}{378}{469}{19}{37}{89}
            {37}19{238}{2357}{2358}46{58}
            6{3478}{3478}{14}{3578}{49}{159}{37}2
            196{78}4{78}253
            2{345}{34}{39}6{359}817
            {37}{3578}{378}{23}{235}1694
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(8, 4, 3))
        assertLogicalSolution(expected, board, ::emptyRectangles)
    }

    @Test
    fun test4() {
        val board = """
            695{237}1{278}{2347}{23478}{2378}
            {137}8{137}4{237}96{237}5
            {37}245{378}69{378}1
            {13579}{3456}{1379}{69}{24578}{24578}{2347}{23478}{23789}
            8{46}2{69}{47}351{79}
            {3579}{345}{379}1{24578}{24578}{2347}6{23789}
            {2345}78{23}9{245}1{235}6
            {2359}{35}{39}{237}618{2357}4
            {2345}168{23457}{2457}{237}9{237}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 3, 7),
            RemoveCandidates(0, 8, 7),
            RemoveCandidates(7, 7, 7),
            RemoveCandidates(8, 4, 2)
        )
        assertLogicalSolution(expected, board, ::emptyRectangles)
    }

    @Test
    fun test5() {
        val board = """
            695{23}1{278}{2347}{23478}{238}
            {137}8{137}4{237}96{237}5
            {37}245{378}69{378}1
            {13579}{3456}{1379}{69}{24578}{24578}{2347}{23478}{23789}
            8{46}2{69}{47}351{79}
            {3579}{345}{379}1{24578}{24578}{2347}6{23789}
            {34}78{23}9{245}1{35}6
            {2359}{35}{39}7618{235}4
            {2345}168{345}{45}{237}9{237}
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(6, 7, 3))
        assertLogicalSolution(expected, board, ::emptyRectangles)
    }
}