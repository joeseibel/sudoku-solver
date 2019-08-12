package sudokusolver.kotlin.logic.tough

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import sudokusolver.kotlin.RemoveCandidates
import sudokusolver.kotlin.parseCellsWithCandidates

internal class XYZWingKtTest {
    /*
     * 0 9 2 | 0 0 1 | 7 5 0
     * 5 0 0 | 2 0 0 | 0 0 8
     * 0 0 0 | 0 3 0 | 2 0 0
     * ------+-------+------
     * 0 7 5 | 0 0 4 | 9 6 0
     * 2 0 0 | 0 6 0 | 0 7 5
     * 0 6 9 | 7 0 0 | 0 3 0
     * ------+-------+------
     * 0 0 8 | 0 9 0 | 0 2 0
     * 7 0 0 | 0 0 3 | 0 8 9
     * 9 0 3 | 8 0 0 | 0 4 0
     */
    @Test
    fun testXYZWing1() {
        val board = """
            {38}92{46}{48}175{346}
            5{134}{1467}2{47}{679}{346}{19}8
            {146}{148}{1467}{4569}3{56789}2{19}{46}
            {38}75{13}{128}496{12}
            2{38}{14}{139}6{89}{148}75
            {14}697{125}{258}{148}3{124}
            {146}{145}8{1456}9{567}{1356}2{1367}
            7{1245}{146}{1456}{1245}3{156}89
            9{125}38{1257}{2567}{156}4{167}
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(5, 6, 1)
        )
        assertIterableEquals(expected, xyzWing(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 6 0 0 | 0 0 0 | 0 0 8
     * 5 0 0 | 9 0 8 | 0 0 7
     * 8 2 0 | 0 0 1 | 0 3 0
     * ------+-------+------
     * 3 4 0 | 2 0 9 | 0 8 0
     * 2 0 0 | 0 8 0 | 3 0 0
     * 1 8 0 | 3 0 7 | 0 2 5
     * ------+-------+------
     * 7 5 0 | 4 0 0 | 0 9 2
     * 9 0 0 | 0 0 5 | 0 0 4
     * 4 0 0 | 0 9 0 | 0 0 3
     */
    @Test
    fun textXYZWing2() {
        val board = """
            6{79}{13479}{57}{23457}{234}{125}{145}8
            5{13}{134}9{2346}8{1246}{146}7
            82{47}{567}{4567}1{4569}3{69}
            34{567}2{15}9{67}8{16}
            2{79}{5679}{15}8{46}3{467}{169}
            18{69}3{46}7{469}25
            75{138}4{136}{36}{168}92
            9{136}{1238}{1678}{12367}5{1678}{167}4
            4{16}{128}{1678}9{26}{15678}{1567}3
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(4, 8, 6)
        )
        assertIterableEquals(expected, xyzWing(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 9 0 0 | 1 0 0 | 0 0 7
     * 3 1 2 | 0 7 0 | 9 8 6
     * 7 8 5 | 9 6 2 | 3 1 4
     * ------+-------+------
     * 1 0 9 | 0 0 7 | 4 0 3
     * 5 0 0 | 0 1 0 | 0 0 8
     * 8 0 7 | 2 0 0 | 1 0 5
     * ------+-------+------
     * 6 9 1 | 7 4 0 | 8 0 2
     * 0 5 3 | 0 0 0 | 0 0 1
     * 0 7 8 | 0 0 1 | 0 0 9
     */
    @Test
    fun testXYZWing3() {
        val board = """
            9{46}{46}1{38}{38}{25}{25}7
            312{45}7{45}986
            785962314
            1{26}9{58}{58}74{26}3
            5{2346}{46}{346}1{3469}{267}{2679}8
            8{346}72{39}{3469}1{69}5
            69174{35}8{35}2
            {24}53{68}{289}{689}{67}{467}1
            {24}78{356}{235}1{56}{3456}9
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(8, 4, 5)
        )
        assertIterableEquals(expected, xyzWing(parseCellsWithCandidates(board)).sorted())
    }

    /*
     * 9 0 0 | 8 5 0 | 0 0 1
     * 8 5 0 | 2 0 1 | 0 0 7
     * 6 1 0 | 0 3 0 | 0 5 8
     * ------+-------+------
     * 4 0 5 | 0 7 8 | 0 1 2
     * 2 8 1 | 0 0 0 | 0 7 0
     * 7 3 0 | 0 1 2 | 5 8 0
     * ------+-------+------
     * 1 0 0 | 0 2 0 | 8 4 3
     * 3 0 0 | 1 8 9 | 7 2 5
     * 5 2 8 | 7 4 3 | 1 9 6
     */
    @Test
    fun testXYZWing4() {
        val board = """
            9{47}{2347}85{467}{24}{36}1
            85{34}2{69}1{49}{36}7
            61{247}{49}3{47}{249}58
            4{69}5{369}78{36}12
            281{34569}{69}{456}{36}7{49}
            73{69}{469}1258{49}
            1{79}{79}{56}2{56}843
            3{46}{46}189725
            528743196
        """.trimIndent().replace("\n", "")
        val expected = listOf(
            RemoveCandidates(3, 3, 9),
            RemoveCandidates(4, 3, 9)
        )
        assertIterableEquals(expected, xyzWing(parseCellsWithCandidates(board)).sorted())
    }
}