import SwiftGraph
import XCTest

final class MedusaTest: XCTestCase {
    func testToDOT() {
        let graph = UnweightedUniqueElementsGraph<CodableLocatedCandidate>()
        let cells = [Cell(row: 0, column: 0), Cell(row: 0, column: 0)].unsolvedCells
        let a = CodableLocatedCandidate(cell: cells[0], candidate: .two)
        let b = CodableLocatedCandidate(cell: cells[1], candidate: .six)
        let aIndex = graph.addVertex(a)
        let bIndex = graph.addVertex(b)
        graph.addEdge(fromIndex: aIndex, toIndex: bIndex)
        let actual = graph.toDOT()
        let expected = """
            strict graph {
              "[0,0] : 2" -- "[0,0] : 6"
            }
            """
        XCTAssertEqual(expected, actual)
    }
    
    func testRule1Test1() {
        let board = """
            {17}9382456{17}
            {147}856{39}{13}{49}{137}2
            2{14}6{139}75{49}{13}8
            321769845
            {469}{46}{49}2583{17}{17}
            578{13}4{13}296
            85{49}{49}16723
            {149}{134}7{349}8265{49}
            {69}{346}25{39}718{49}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 0, value: 4),
            BoardModification(row: 1, column: 4, value: 3),
            BoardModification(row: 1, column: 6, value: 9),
            BoardModification(row: 2, column: 1, value: 1),
            BoardModification(row: 2, column: 3, value: 9),
            BoardModification(row: 2, column: 6, value: 4),
            BoardModification(row: 7, column: 0, value: 1),
            BoardModification(row: 7, column: 3, value: 3),
            BoardModification(row: 8, column: 1, value: 3),
            BoardModification(row: 8, column: 4, value: 9)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule1)
    }
    
    func testRule1Test2() {
        let board = """
            {567}{267}{26}9{16}843{15}
            {59}{359}47{13}268{15}
            {36}81{36}54{79}{79}2
            {78}{47}5{68}{46}3129
            {169}{469}{69}52{17}3{47}8
            {12}{23}{38}{48}9{17}56{47}
            {256}{56}{36}{24}7981{34}
            {38}17{23}{48}5{29}{49}6
            4{29}{289}1{38}6{27}5{37}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 1, value: 7),
            BoardModification(row: 0, column: 4, value: 1),
            BoardModification(row: 0, column: 8, value: 5),
            BoardModification(row: 1, column: 4, value: 3),
            BoardModification(row: 1, column: 8, value: 1),
            BoardModification(row: 2, column: 0, value: 3),
            BoardModification(row: 2, column: 3, value: 6),
            BoardModification(row: 2, column: 6, value: 9),
            BoardModification(row: 2, column: 7, value: 7),
            BoardModification(row: 3, column: 0, value: 7),
            BoardModification(row: 3, column: 1, value: 4),
            BoardModification(row: 3, column: 3, value: 8),
            BoardModification(row: 3, column: 4, value: 6),
            BoardModification(row: 4, column: 0, value: 1),
            BoardModification(row: 4, column: 5, value: 7),
            BoardModification(row: 4, column: 7, value: 4),
            BoardModification(row: 5, column: 0, value: 2),
            BoardModification(row: 5, column: 1, value: 3),
            BoardModification(row: 5, column: 2, value: 8),
            BoardModification(row: 5, column: 3, value: 4),
            BoardModification(row: 5, column: 5, value: 1),
            BoardModification(row: 5, column: 8, value: 7),
            BoardModification(row: 6, column: 2, value: 3),
            BoardModification(row: 6, column: 3, value: 2),
            BoardModification(row: 6, column: 8, value: 4),
            BoardModification(row: 7, column: 0, value: 8),
            BoardModification(row: 7, column: 3, value: 3),
            BoardModification(row: 7, column: 4, value: 4),
            BoardModification(row: 7, column: 6, value: 2),
            BoardModification(row: 7, column: 7, value: 9),
            BoardModification(row: 8, column: 4, value: 8),
            BoardModification(row: 8, column: 6, value: 7),
            BoardModification(row: 8, column: 8, value: 3)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule1)
    }
    
    func testRule2Test1() {
        let board = """
            3{168}{1679}{189}52{46}{479}{789}
            25{679}3{489}{49}{67}1{789}
            {19}{18}46{189}7523
            {16}932{467}{14}8{47}5
            57{126}{89}{689}{149}{1249}3{19}
            4{12}8{79}35{179}6{127}
            {1679}{126}54{179}83{79}{1279}
            {179}3{129}5{179}6{1279}84
            84{19}{179}23{179}56
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 6, value: 4),
            BoardModification(row: 1, column: 6, value: 6),
            BoardModification(row: 3, column: 4, value: 7),
            BoardModification(row: 3, column: 7, value: 4),
            BoardModification(row: 4, column: 5, value: 4),
            BoardModification(row: 5, column: 3, value: 9),
            BoardModification(row: 8, column: 3, value: 7)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule2)
    }
    
    func testRule2Test2() {
        let board = """
            748156{39}{29}{23}
            359284{67}1{67}
            612379458
            {19}86{49}{149}3275
            47{13}5{16}2{368}{68}9
            2{39}57{69}814{36}
            5{269}7{489}{49}1{689}3{246}
            {89}{29}46375{289}1
            {189}{369}{13}{489}25{6789}{689}{467}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 6, value: 9),
            BoardModification(row: 0, column: 7, value: 2),
            BoardModification(row: 0, column: 8, value: 3),
            BoardModification(row: 3, column: 0, value: 9),
            BoardModification(row: 3, column: 4, value: 1),
            BoardModification(row: 4, column: 2, value: 1),
            BoardModification(row: 4, column: 4, value: 6),
            BoardModification(row: 4, column: 6, value: 3),
            BoardModification(row: 5, column: 1, value: 3),
            BoardModification(row: 5, column: 4, value: 9),
            BoardModification(row: 5, column: 8, value: 6),
            BoardModification(row: 6, column: 8, value: 2),
            BoardModification(row: 7, column: 1, value: 2),
            BoardModification(row: 8, column: 0, value: 1),
            BoardModification(row: 8, column: 2, value: 3)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule2)
    }
    
    func testRule3Test1() {
        let board = """
            29{1467}{56}{57}{46}83{156}
            {145}{18}{1468}{3568}2{3468}97{156}
            {357}{378}{678}1{578}94{56}2
            845761293
            6{123}{12}{2389}{89}{238}547
            {37}{237}9{23}45{16}{16}8
            9{128}34{158}7{16}{1256}{56}
            {14}6{1248}{258}3{28}7{125}9
            {17}5{127}{269}{19}{26}384
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 2, column: 1, candidates: 8)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule3)
    }
    
    func testRule3Test2() {
        let board = """
            9{35}8{13}2{134}{45}76
            6{25}{24}{389}{359}71{48}{389}
            17{34}{3689}{34569}{34689}{59}2{389}
            {78}{28}54{36}{36}{27}91
            391782{46}{46}5
            46{27}{19}{19}583{27}
            {78}4{37}{123689}{1369}{13689}{269}5{289}
            5{38}6{239}{349}{349}{279}1{2789}
            21957{68}3{68}4
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 7, column: 8, candidates: 2, 9)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule3)
    }
    
    func testRule3Test3() {
        let board = """
            {2567}{2567}{26}9{16}843{15}
            {359}{359}47{13}268{15}
            {36}81{36}54{79}{79}2
            {78}{47}5{468}{46}3129
            {169}{469}{69}52{17}3{47}8
            {1238}{234}{238}{48}9{17}56{47}
            {2356}{2356}{236}{24}7981{34}
            {389}17{234}{348}5{29}{49}6
            4{239}{2389}1{38}6{279}5{37}
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 8, column: 6, candidates: 9)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule3)
    }
    
    func testRule4Test1() {
        let board = """
            1{79}{29}{278}56{478}{489}3
            {256}43{1278}9{78}{1578}{568}{68}
            8{679}{569}{17}43{157}{569}2
            {47}3{48}56{789}21{49}
            95{68}421{68}37
            {467}21{78}3{789}{4568}{4568}{469}
            31798{24}{46}{246}5
            {2456}{68}{245}31{245}97{48}
            {245}{89}{2459}67{245}3{248}1
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 0, candidates: 6),
            BoardModification(row: 2, column: 7, candidates: 6)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule4)
    }
    
    func testRule4Test2() {
        let board = """
            1{79}{29}{278}56{478}{489}3
            {25}43{1278}9{78}{1578}{568}{68}
            8{679}{569}{17}43{157}{59}2
            {47}3{48}56{789}21{49}
            95{68}421{68}37
            {467}21{78}3{789}{4568}{4568}{469}
            31798{24}{46}{246}5
            {2456}{68}{245}31{245}97{48}
            {245}{89}{2459}67{245}3{248}1
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 5, candidates: 8),
            BoardModification(row: 3, column: 8, candidates: 4),
            BoardModification(row: 5, column: 6, candidates: 6),
            BoardModification(row: 5, column: 7, candidates: 6),
            BoardModification(row: 7, column: 2, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule4)
    }
    
    func testRule4Test3() {
        let board = """
            9{35}8{13}2{134}{45}76
            6{235}{234}{389}{359}71{48}{389}
            17{34}{3689}{34569}{34689}{59}2{389}
            {78}{28}54{36}{36}{27}91
            391782{46}{46}5
            46{27}{19}{19}583{27}
            {78}4{37}{123689}{1369}{13689}{269}5{289}
            5{38}6{239}{349}{349}{279}1{2789}
            21957{68}3{68}4
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 1, candidates: 3),
            BoardModification(row: 1, column: 2, candidates: 3)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule4)
    }
    
    func testRule4Test4() {
        let board = """
            9{35}8{13}2{134}{45}76
            6{25}{24}{389}{359}71{48}{389}
            17{34}{3689}{34569}{34689}{59}2{389}
            {78}{28}54{36}{36}{27}91
            391782{46}{46}5
            46{27}{19}{19}583{27}
            {78}4{37}{123689}{1369}{13689}{269}5{289}
            5{38}6{239}{349}{349}{279}1{78}
            21957{68}3{68}4
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 6, column: 8, candidates: 8)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule4)
    }
    
    func testRule5Test1() {
        let board = """
            9234{68}7{68}15
            876{13}5{13}924
            5{14}{14}2{689}{69}{678}3{78}
            769{358}2{35}14{38}
            432{168}{167}{16}{78}59
            185{39}{79}426{37}
            {36}98{56}42{35}71
            2{15}7{159}3{159}486
            {36}{145}{14}7{16}8{35}92
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 4, candidates: 8),
            BoardModification(row: 2, column: 6, candidates: 6),
            BoardModification(row: 4, column: 3, candidates: 6),
            BoardModification(row: 4, column: 4, candidates: 1)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule5)
    }
    
    func testRule5Test2() {
        let board = """
            3{168}{1679}{189}52{4679}{479}{789}
            25{679}3{489}{49}{67}1{789}
            {19}{18}46{189}7523
            {16}932{467}{14}8{47}5
            57{126}{89}{4689}{149}{1249}3{19}
            4{12}8{79}35{179}6{127}
            {1679}{126}54{179}83{79}{1279}
            {179}3{129}5{179}6{1279}84
            84{19}{179}23{179}56
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 6, candidates: 7, 9),
            BoardModification(row: 4, column: 4, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule5)
    }
    
    func testRule5Test3() {
        let board = """
            9{35}8{13}2{134}{45}76
            6{235}{234}{389}{3459}71{48}{389}
            17{34}{3689}{34569}{34689}{59}2{389}
            {78}{28}54{36}{36}{27}91
            391782{46}{46}5
            46{27}{19}{19}583{27}
            {78}4{37}{123689}{1369}{13689}{269}5{289}
            5{38}6{239}{349}{349}{279}1{2789}
            21957{68}3{68}4
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 4, candidates: 4),
            BoardModification(row: 7, column: 8, candidates: 2)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule5)
    }
    
    func testRule5Test4() {
        let board = """
            9{35}8{13}2{134}{45}76
            6{25}{24}{389}{359}71{48}{389}
            17{34}{3689}{34569}{4689}{59}2{389}
            {78}{28}54{36}{36}{27}91
            391782{46}{46}5
            46{27}{19}{19}583{27}
            {78}4{37}{12368}{136}{1368}{69}5{29}
            5{38}6{239}{349}{49}{27}1{78}
            21957{68}3{68}4
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 4, candidates: 3),
            BoardModification(row: 6, column: 3, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule5)
    }
    
    func testRule5Test5() {
        let board = """
            {28}19{36}4{38}75{26}
            {78}5{24}{68}{79}{19}{13}{236}{246}
            {47}36{17}52{14}89
            {16}8542{19}{69}73
            {24}97{38}{38}6{24}15
            3{246}{124}{17}{79}5{69}{24}8
            {16}{247}{124}5{36}{37}89{247}
            5{267}8914{23}{236}{267}
            9{47}32{68}{78}5{46}1
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 7, candidates: 2),
            BoardModification(row: 1, column: 8, candidates: 6),
            BoardModification(row: 5, column: 1, candidates: 4),
            BoardModification(row: 5, column: 2, candidates: 2),
            BoardModification(row: 6, column: 2, candidates: 4),
            BoardModification(row: 6, column: 8, candidates: 2, 7),
            BoardModification(row: 7, column: 1, candidates: 2, 7),
            BoardModification(row: 7, column: 7, candidates: 6)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule5)
    }
    
    func testRule5Test6() {
        let board = """
            748156{39}{29}{23}
            359284{67}1{67}
            612379458
            {19}86{49}{149}3275
            47{13}5{16}2{368}{68}9
            {29}{239}57{69}814{36}
            5{269}7{489}{49}1{689}3{246}
            {289}{29}46375{289}1
            {189}{369}{13}{489}25{6789}{689}{467}
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 5, column: 1, candidates: 2)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule5)
    }
    
    func testRule6Test1() {
        let board = """
            986721345
            3{12}4956{18}{128}7
            {25}{125}7{48}3{48}96{12}
            {248}73{248}65{148}{18}9
            69{28}{248}17{458}{58}3
            1{45}{58}39{48}276
            {2458}{245}{258}679{15}3{128}
            {258}691437{25}{28}
            731582694
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 1, value: 1),
            BoardModification(row: 1, column: 7, value: 2),
            BoardModification(row: 2, column: 8, value: 1),
            BoardModification(row: 4, column: 6, value: 5),
            BoardModification(row: 4, column: 7, value: 8),
            BoardModification(row: 6, column: 6, value: 1),
            BoardModification(row: 7, column: 7, value: 5)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule6)
    }
    
    func testRule6Test2() {
        let board = """
            9{35}8{13}2{134}{45}76
            6{25}{24}{389}{359}71{48}{389}
            17{34}{3689}{4569}{469}{59}2{389}
            {78}{28}54{36}{36}{27}91
            391782{46}{46}5
            46{27}{19}{19}583{27}
            {78}4{37}{1236}{136}{1368}{69}5{29}
            5{38}6{239}{349}{49}{27}1{78}
            21957{68}3{68}4
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 1, value: 5),
            BoardModification(row: 0, column: 6, value: 4),
            BoardModification(row: 1, column: 1, value: 2),
            BoardModification(row: 1, column: 2, value: 4),
            BoardModification(row: 1, column: 4, value: 5),
            BoardModification(row: 1, column: 7, value: 8),
            BoardModification(row: 2, column: 2, value: 3),
            BoardModification(row: 2, column: 6, value: 5),
            BoardModification(row: 3, column: 0, value: 7),
            BoardModification(row: 3, column: 1, value: 8),
            BoardModification(row: 3, column: 6, value: 2),
            BoardModification(row: 4, column: 6, value: 6),
            BoardModification(row: 4, column: 7, value: 4),
            BoardModification(row: 5, column: 2, value: 2),
            BoardModification(row: 5, column: 8, value: 7),
            BoardModification(row: 6, column: 0, value: 8),
            BoardModification(row: 6, column: 2, value: 7),
            BoardModification(row: 6, column: 6, value: 9),
            BoardModification(row: 6, column: 8, value: 2),
            BoardModification(row: 7, column: 1, value: 3),
            BoardModification(row: 7, column: 3, value: 2),
            BoardModification(row: 7, column: 6, value: 7),
            BoardModification(row: 7, column: 8, value: 8),
            BoardModification(row: 8, column: 5, value: 8),
            BoardModification(row: 8, column: 7, value: 6)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule6)
    }
    
    func testRule6Test3() {
        let board = """
            2{147}{179}35{679}{4679}8{69}
            5{47}{79}{269}81{24679}{2467}3
            836{29}4{79}{2579}1{59}
            4{157}{17}83{69}{5679}{567}2
            6{578}2{59}143{57}{589}
            9{58}3{56}72{4568}{456}1
            325468197
            768193{25}{25}4
            194725{68}3{68}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 3, value: 6),
            BoardModification(row: 3, column: 5, value: 6),
            BoardModification(row: 3, column: 6, value: 9),
            BoardModification(row: 4, column: 3, value: 9),
            BoardModification(row: 5, column: 3, value: 5)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: medusaRule6)
    }
}
