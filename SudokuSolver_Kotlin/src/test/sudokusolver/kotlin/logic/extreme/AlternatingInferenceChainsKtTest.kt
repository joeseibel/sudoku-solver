package sudokusolver.kotlin.logic.extreme

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.SetValue
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class AlternatingInferenceChainsKtTest {
    @Test
    fun rule1Test1() {
        val board = """
            {179}384{27}{125}{259}{1569}{269}
            2{47}{17}9{67}{156}{45}38
            {149}6538{12}7{19}{249}
            {578}{589}{279}{2678}3{269}14{269}
            6{4789}3{278}1{249}{289}{79}5
            {478}1{279}{2678}5{2469}{289}{679}3
            {78}{78}4593621
            32{169}{16}{46}8{459}{59}7
            {15}{59}{169}{126}{246}738{49}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 0, 1),
            RemoveCandidates(1, 2, 7),
            RemoveCandidates(1, 5, 1),
            RemoveCandidates(2, 0, 1),
            RemoveCandidates(3, 0, 7),
            RemoveCandidates(4, 1, 7),
            RemoveCandidates(5, 0, 7),
            RemoveCandidates(8, 4, 6)
        )
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule1)
    }
    
    @Test
    fun rule1Test2() {
        val board = """
            5{489}{469}{1248}7{248}{124}{2469}3
            {39}1{3479}6{345}{2345}{2457}8{4579}
            {368}{478}2{13458}{13458}9{1457}{456}{14567}
            {123}6{1345}{234578}{13458}{234578}9{345}{457}
            7{459}{13459}{1345}{134569}{3456}8{3456}2
            {239}{2459}8{23457}{34569}{3457}{3457}1{4567}
            {128}{2578}{157}9{3458}{34578}6{2345}{145}
            {2689}3{569}{458}{4568}1{245}7{459}
            4{579}{15679}{357}2{3567}{135}{359}8
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 7, 4, 9),
            RemoveCandidates(7, 0, 8, 9)
        )
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule1)
    }

    @Test
    fun rule1Test3() {
        val board = """
            {4589}{48}613{479}{57}2{789}
            {89}31{79}5264{789}
            72{459}{69}8{469}3{59}1
            26{49}5718{39}{34}
            {159}{57}84632{179}{79}
            {14}{47}3298{147}65
            3{458}{245}{789}{12}{579}{1457}{157}6
            {456}973{12}{56}{145}8{24}
            {568}1{25}{68}4{567}9{357}{23}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 0, 4),
            RemoveCandidates(2, 5, 9)
        )
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule1)
    }

    @Test
    fun rule2Test1() {
        val board = """
            {179}384{27}{125}{259}{1569}{269}
            2{47}{17}9{67}{156}{45}38
            {149}6538{12}7{19}{249}
            {578}{589}{279}{2678}3{269}14{269}
            6{4789}3{278}1{249}{289}{79}5
            {478}1{279}{2678}5{2469}{289}{679}3
            {78}{78}4593621
            32{169}{16}{46}8{459}{59}7
            {15}{59}{169}{126}{246}738{49}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            SetValue(0, 4, 7),
            SetValue(8, 8, 4)
        )
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule2)
    }

    @Test
    fun rule2Test2() {
        val board = """
            7{158}{168}9{136}{168}42{1358}
            {156}92{34}{346}{18}{56}{367}{1578}
            {468}{148}32579{168}{18}
            3{16}478{56}2{156}9
            97{18}{45}{46}2{36}{135}{138}
            2{68}51937{68}4
            {58}{358}96241{37}{57}
            {146}{34}{16}{35}7{15}892
            {15}278{13}9{35}46
        """.trimIndent().replace("\n", "")
        val expected = listOf(SetValue(0, 1, 5))
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule2)
    }

    @Test
    fun rule2Test3() {
        val board = """
            869{47}51{247}{23}{34}
            347286915
            521{479}{79}3{47}68
            953{68}{26}{28}147
            784319{25}{25}6
            612547389
            {14}{37}8{1679}{679}5{46}{39}2
            {124}95{168}3{28}{468}7{14}
            {12}{37}6{1789}{279}4{58}{359}{13}
        """.trimIndent().replace("\n", "")
        val expected = listOf(SetValue(8, 6, 8))
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule2)
    }
    
    @Test
    fun rule2Test4() {
        val board = """
            {689}{145}3{145}2{46}7{15689}{5689}
            {69}{145}27{45}83{1569}{569}
            {68}{15}7{135}9{36}{12}{12568}4
            3942{58}16{58}7
            1256{48}7{49}3{89}
            786{45}39{124}{125}{25}
            439862571
            261975843
            578{34}1{34}{29}{269}{269}
        """.trimIndent().replace("\n", "")
        val expected = listOf(SetValue(8, 5, 4))
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule2)
    }
    
    @Test
    fun rule2Test5() {
        val board = """
            9{1267}3{267}54{1267}{127}8
            5{1267}{1267}{2367}8{3679}{1267}{12379}4
            48{267}{2367}1{3679}{2567}{23579}{279}
            1{379}542{37}86{79}
            84{279}56{17}3{1279}{1279}
            6{237}{27}{378}9{1378}4{127}5
            3{16}894{56}{1257}{1257}{127}
            2{169}{169}{68}7{568}{15}43
            754132986
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            SetValue(2, 7, 5),
            SetValue(7, 6, 5)
        )
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule2)
    }
    
    @Test
    fun rule2Test6() {
        val board = """
            415{69}{69}2387
            382147{69}5{69}
            796853142
            95{34}{2347}16{27}{237}8
            17{38}{239}{289}54{2369}{369}
            62{348}{3479}{789}{48}{79}15
            {258}{346}7{2456}{268}9{268}{236}1
            {258}{346}9{24567}{2678}1{2678}{2367}{346}
            {28}{46}1{2467}3{48}5{2679}{469}
        """.trimIndent().replace("\n", "")
        val expected = listOf(SetValue(8, 7, 9))
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule2)
    }
    
    @Test
    fun rule2Test7() {
        val board = """
            {23468}1{234}{568}{689}{25}{79}{48}{479}
            7{28}94{18}{12}365
            {468}{458}{45}7{689}32{148}{149}
            56{237}1{23}489{27}
            9{234}1{358}{2358}76{45}{24}
            {24}{247}89{25}6{17}{145}3
            {48}{478}62{147}953{18}
            19{357}{35}{357}8426
            {2348}{23458}{2345}{356}{1456}{15}{19}7{189}
        """.trimIndent().replace("\n", "")
        val expected = listOf(SetValue(4, 3, 8))
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule2)
    }

    @Test
    fun rule3Test1() {
        val board = """
            {4589}{48}613{479}{57}2{789}
            {89}31{79}5264{789}
            72{459}{69}8{469}3{59}1
            26{49}5718{39}{34}
            {159}{57}84632{179}{79}
            {14}{47}3298{147}65
            3{458}{245}{789}{12}{579}{1457}{157}6
            {456}973{12}{56}{145}8{24}
            {568}1{25}{678}4{567}9{357}{23}
        """.trimIndent().replace("\n", "")
        val expected = listOf(RemoveCandidates(8, 3, 7))
        assertLogicalSolution(expected, board, ::alternatingInferenceChainsRule3)
    }
}