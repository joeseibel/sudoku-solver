package sudokusolver.kotlin.logic.diabolical

import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.logic.assertLogicalSolution

internal class UniqueRectanglesKtTest {
    @Test
    fun uniqueRectanglesType1Test1() {
        val board = """
            {79}{79}6324815
            85{23}691{24}7{34}
            {24}{234}1785{29}{369}{36}
            {1259}{129}4{59}3768{29}
            38{59}{59}62147
            {29}6741835{29}
            {24569}{249}{259}173{459}{69}8
            {579}{379}{359}846{59}21
            {146}{14}82597{36}{346}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 0, 2, 9)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType1)
    }

    @Test
    fun uniqueRectanglesType1Test2() {
        val board = """
            {589}{15}{169}{69}3{25}{24578}{1578}{12457}
            37418{25}{25}69
            {589}2{169}{69}74{58}3{15}
            {59}8{139}4{12}7{235}{159}6
            6{145}{137}8{12}9{23457}{157}{12457}
            2{14}{179}356{478}{1789}{147}
            4982631{57}{57}
            732591648
            165748923
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 6, 2, 5)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType1)
    }

    @Test
    fun uniqueRectanglesType1Test3() {
        val board = """
            {4569}8{469}72{59}{456}13
            {345679}{45679}{234679}8{46}1{456}{49}{27}
            1{45679}{24679}3{46}{59}{4568}{489}{27}
            {45789}{4579}{4789}2{78}6{48}31
            {78}315{78}4269
            {468}2{468}9137{48}5
            2{46}{46}137958
            {37}{17}5698{13}24
            {389}{19}{389}452{13}76
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 0, 7, 8)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType1)
    }

    @Test
    fun uniqueRectanglesType1Test4() {
        val board = """
            12{58}{568}{568}7943
            96{35}{345}1{34}872
            47{38}9{238}{238}156
            3{48}71{468}9{46}25
            6{48}9{248}{248}5731
            512{346}7{346}{46}98
            7{59}1{3568}{35689}{368}2{68}4
            8{59}6{245}{2459}{24}317
            2347{68}15{68}9
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(6, 4, 6, 8)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType1)
    }

    @Test
    fun uniqueRectanglesType2Test1() {
        val board = """
            42{157}9{157}{157}386
            {135}6{135}2{1358}{158}794
            8{37}9{34}6{47}251
            7{14}{168}{468}{489}3{19}25
            9{45}{58}1{478}26{47}3
            2{134}{136}5{479}{4679}{19}{47}8
            {13}{139}4{38}2{89}567
            6827{15}{15}439
            {35}{579}{57}{346}{349}{469}812
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 2, 7),
            RemoveCandidates(2, 5, 7)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType2)
    }

    @Test
    fun uniqueRectanglesType2Test2() {
        val board = """
            {145}7{146}9{156}3{456}82
            {13458}{35689}{13469}{1568}2{168}{345679}{3579}{347}
            2{35689}{369}4{568}7{3569}{359}1
            6{34}5{78}{38}21{347}9
            {13}2{139}{57}{359}4{378}6{378}
            7{349}8{16}{39}{16}2{34}5
            9{38}{347}2{148}5{3478}{137}6
            {3458}{356}2{168}7{168}{34589}{1359}{348}
            {458}1{467}3{468}9{4578}2{478}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(6, 4, 8),
            RemoveCandidates(7, 0, 8),
            RemoveCandidates(7, 6, 8),
            RemoveCandidates(7, 8, 8),
            RemoveCandidates(8, 4, 8)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType2)
    }

    @Test
    fun uniqueRectanglesType2Test3() {
        val board = """
            {589}{15}{169}{69}3{25}{478}{1578}{12457}
            37418{25}{25}69
            {589}2{169}{69}74{58}3{15}
            {59}8{139}4{12}7{235}{159}6
            6{145}{137}8{12}9{23457}{157}{12457}
            2{14}{179}356{478}{1789}{147}
            4982631{57}{57}
            732591648
            165748923
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 1, 1),
            RemoveCandidates(3, 2, 1),
            RemoveCandidates(4, 2, 1),
            RemoveCandidates(5, 2, 1)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType2)
    }

    @Test
    fun uniqueRectanglesType2Test4() {
        val board = """
            319856{47}{47}2
            245973{16}{16}8
            {68}7{68}412935
            98{67}34125{67}
            {67}34529{167}8{167}
            1527683{49}{49}
            {48}6{18}2375{149}{149}
            {457}23{16}9{45}8{1467}{1467}
            {457}9{17}{16}8{45}{1467}23
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(4, 0, 7),
            RemoveCandidates(6, 2, 1),
            RemoveCandidates(7, 7, 1),
            RemoveCandidates(7, 8, 1),
            RemoveCandidates(8, 2, 7),
            RemoveCandidates(8, 6, 1)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType2)
    }

    @Test
    fun uniqueRectanglesType2Test5() {
        val board = """
            654728{19}{139}{39}
            321964{58}7{58}
            978315642
            8{34}7652{49}{39}1
            1{36}{256}497{258}{236}{358}
            {245}9{256}831{2457}{26}{457}
            {247}83{12}{47}9{1247}56
            {247}{146}{269}5{47}3{12479}8{479}
            {457}{14}{59}{12}863{129}{479}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(5, 0, 2),
            RemoveCandidates(7, 2, 2)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType2)
    }

    @Test
    fun uniqueRectanglesType2BTest() {
        val board = """
            {27}4186539{27}
            {278}9{257}{13}4{13}{578}6{278}
            {68}3{56}7924{58}1
            {36}28{135}{357}{137}94{56}
            519624{78}{78}3
            {346}7{46}9{35}821{56}
            15{347}{34}8{37}629
            {247}6{247}{45}19{578}3{478}
            98{347}2{357}61{57}{47}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 6, 8)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType2)
    }

    @Test
    fun uniqueRectanglesType2CTest() {
        val board = """
            8{247}9{234}{146}{26}{37}5{1367}
            53{16}8{146}7{269}{1246}{29}
            {146}{24}{167}{2345}9{256}8{146}{1367}
            2946{57}813{57}
            78{36}9{235}1{256}{26}4
            {36}15{27}{237}4{267}98
            {1349}{47}2{457}8{569}{35}{16}{13569}
            {49}581{46}3{29}7{269}
            {139}6{137}{257}{27}{259}48{1359}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(0, 8, 6),
            RemoveCandidates(2, 8, 6)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType2)
    }

    @Test
    fun uniqueRectanglesType3Test() {
        val board = """
            {69}{69}{128}5{12}347{18}
            5{37}{12}8{127}4{139}6{39}
            4{37}{18}{17}96{138}52
            857{123}{13}96{12}4
            3246{18}759{18}
            {19}{19}6{24}{48}5{28}37
            285{37}61{379}4{39}
            {167}{16}9{347}{347}8{1237}{12}5
            {17}43952{17}86
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(2, 6, 1),
            RemoveCandidates(7, 6, 1, 7)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType3)
    }

    @Test
    fun uniqueRectanglesType3BTest() {
        val board = """
            419{78}2{37}{3578}{35}6
            {25}6{25}1{378}9{3478}{34}{347}
            {78}3{78}465921
            {56}9{345}2{37}1{3467}8{3457}
            {38}{48}1{678}5{367}29{347}
            {256}7{2358}9{38}4{36}1{35}
            {138}{48}65{14}2{134}79
            {17}5{47}398{14}62
            92{34}{67}{14}{67}{1345}{345}8
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 4, 3),
            RemoveCandidates(4, 1, 8),
            RemoveCandidates(4, 8, 3)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType3)
    }

    @Test
    fun uniqueRectanglesType3BWithTriplePseudoCellsTest1() {
        val board = """
            7529{36}8{16}4{136}
            3{48}{48}21{56}{79}{79}{56}
            {69}1{69}{3457}{345}{357}28{35}
            {145}63{457}82{47}{17}9
            {14}27{346}9{36}5{16}8
            8{49}{459}1{456}{567}{467}32
            271{356}{356}98{56}4
            {469}{489}{4689}{56}213{569}7
            {569}3{569}874{169}2{16}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(7, 0, 6, 9)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType3BWithTriplePseudoCells)
    }

    @Test
    fun uniqueRectanglesType3BWithTriplePseudoCellsTest2() {
        val board = """
            654728{19}{139}{39}
            321964{58}7{58}
            978315642
            8{34}7652{49}{39}1
            1{36}{256}497{258}{236}{358}
            {45}9{256}831{2457}{26}{457}
            {247}83{12}{47}9{1247}56
            {247}{146}{69}5{47}3{12479}8{479}
            {457}{14}{59}{12}863{129}{479}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 7, 3),
            RemoveCandidates(4, 2, 2, 6),
            RemoveCandidates(5, 6, 2)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType3BWithTriplePseudoCells)
    }

    @Test
    fun uniqueRectanglesType4Test1() {
        val board = """
            {79}{79}6324815
            85{23}691{24}7{34}
            {24}{234}1785{29}{369}{36}
            {15}{129}4{59}3768{29}
            38{59}{59}62147
            {29}6741835{29}
            {24569}{249}{259}173{459}{69}8
            {579}{379}{359}846{59}21
            {146}{14}82597{36}{346}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(7, 0, 9),
            RemoveCandidates(7, 1, 9)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType4)
    }

    @Test
    fun uniqueRectanglesType4Test2() {
        val board = """
            {4569}8{469}72{59}{456}13
            {345679}{45679}{234679}8{46}1{456}{49}{27}
            1{45679}{24679}3{46}{59}{4568}{489}{27}
            {459}{4579}{4789}2{78}6{48}31
            {78}315{78}4269
            {468}2{468}9137{48}5
            2{46}{46}137958
            {37}{17}5698{13}24
            {389}{19}{389}452{13}76
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 2, 7),
            RemoveCandidates(2, 2, 7)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType4)
    }

    @Test
    fun uniqueRectanglesType4Test3() {
        val board = """
            12{58}{568}{568}7943
            96{35}{345}1{34}872
            47{38}9{238}{238}156
            3{48}71{468}9{46}25
            6{48}9{248}{248}5731
            512{346}7{346}{46}98
            7{59}1{3568}{359}{368}2{68}4
            8{59}6{245}{2459}{24}317
            2347{68}15{68}9
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(6, 4, 5),
            RemoveCandidates(7, 4, 5)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType4)
    }

    @Test
    fun uniqueRectanglesType4Test4() {
        val board = """
            31{789}2{789}{89}645
            {26789}{78}5{3679}4{689}1{28}{37}
            {2678}4{678}{1367}5{168}9{28}{37}
            {167}32{1567}{17}{1456}{47}98
            {178}5{478}{179}{1789}{12489}{247}36
            {678}9{4678}{67}3{2468}{247}51
            421863579
            {59}63{59}27814
            {5789}{78}{789}4{19}{159}362
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(1, 0, 8),
            RemoveCandidates(1, 3, 7),
            RemoveCandidates(2, 0, 8),
            RemoveCandidates(2, 3, 7)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType4)
    }

    @Test
    fun uniqueRectanglesType4Test5() {
        val board = """
            173924{58}{58}6
            249865317
            856173429
            96{28}{237}5{278}{178}4{138}
            73{28}41{268}{68}95
            514{37}9{68}{678}{378}2
            6{28}1{27}49{2578}{3578}{38}
            4{289}563{27}{12789}{78}{18}
            3{29}7581{29}64
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(6, 6, 8),
            RemoveCandidates(6, 7, 8),
            RemoveCandidates(7, 1, 2),
            RemoveCandidates(7, 6, 2)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType4)
    }

    @Test
    fun uniqueRectanglesType4BTest1() {
        val board = """
            748359126
            {359}{59}172684{39}
            {39}264{18}{18}7{39}5
            2{56}{39}{169}4{15}{359}87
            {56}74{689}3{258}{259}{169}{19}
            18{39}{69}7{25}{25}{369}4
            4{39}2{15}{19}76{1359}8
            {69}17{58}{689}34{59}2
            8{369}52{169}4{39}7{139}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(4, 5, 5),
            RemoveCandidates(4, 6, 5)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType4)
    }

    @Test
    fun uniqueRectanglesType4BTest2() {
        val board = """
            173924{58}{58}6
            249865317
            856173429
            96{28}{237}5{278}{178}4{138}
            73{28}41{268}{68}95
            514{37}9{68}{678}{378}2
            6{28}1{27}49{257}{357}{38}
            4{289}563{27}{12789}{78}{18}
            3{29}7581{29}64
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(7, 1, 2),
            RemoveCandidates(7, 6, 2)
        )
        assertLogicalSolution(expected, board, ::uniqueRectanglesType4)
    }
}