import SwiftGraph
import XCTest

final class GroupedXCyclesTest: XCTestCase {
    func testToDOT() {
        let graph = WeightedUniqueElementsGraph<Node, Strength>()
        let a = Node.cell([Cell(row: 6, column: 1)].unsolvedCells.first!)
        let bCells = [Cell(row: 6, column: 6), Cell(row: 6, column: 7), Cell(row: 6, column: 8)].unsolvedCells
        let b = Node(rowGroup: bCells)
        let c = Node(columnGroup: [Cell(row: 6, column: 2), Cell(row: 8, column: 2)].unsolvedCells)
        let aIndex = graph.addVertex(a)
        let bIndex = graph.addVertex(b)
        let cIndex = graph.addVertex(c)
        graph.addEdge(fromIndex: aIndex, toIndex: bIndex, weight: .weak)
        graph.addEdge(fromIndex: aIndex, toIndex: cIndex, weight: .strong)
        let actual = graph.toDOT(candidate: .eight)
        let expected = """
            strict graph 8 {
              "[6,1]" -- "{[6,6], [6,7], [6,8]}" [style = dashed]
              "[6,1]" -- "{[6,2], [8,2]}"
            }
            """
        XCTAssertEqual(expected, actual)
    }
    
    func testRule1Test1() {
        let board = """
            185{49}2637{49}
            {234}6{234}{3579}{134}{1357}{2458}{28}{2589}
            {234}97{345}{34}81{26}{2456}
            {4678}1{48}{348}52{68}9{37}
            {245789}{27}{2489}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {23789}{27}{2389}{357}{1348}{13457}{2468}{12368}{123468}
            {38}5629{134}7{138}{1348}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 3, candidates: 4),
            BoardModification(row: 2, column: 8, candidates: 4),
            BoardModification(row: 7, column: 5, candidates: 4),
            BoardModification(row: 7, column: 8, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule1)
    }
    
    func testRule1Test2() {
        let board = """
            3{279}1{89}{258}4{259}6{257}
            8{279}4{69}{256}{59}{2359}1{2357}
            56{29}713{289}{289}4
            {147}3{578}{46}{56}2{16}{78}9
            {147}{128}{2578}{346}9{578}{16}{2378}{2358}
            6{289}{25789}1{3578}{578}{2358}4{2358}
            {17}{18}{378}546{2389}{2389}{238}
            256{389}{378}{789}4{38}1
            94{38}2{38}1756
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 7, candidates: 8),
            BoardModification(row: 4, column: 2, candidates: 8),
            BoardModification(row: 4, column: 7, candidates: 8),
            BoardModification(row: 5, column: 2, candidates: 8),
            BoardModification(row: 5, column: 6, candidates: 8),
            BoardModification(row: 6, column: 2, candidates: 8),
            BoardModification(row: 6, column: 6, candidates: 8),
            BoardModification(row: 6, column: 7, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule1)
    }
    
    func testRule1Test3() {
        let board = """
            3{279}1{89}{258}4{259}6{257}
            8{279}4{69}{256}{59}{2359}1{2357}
            56{29}7138{29}4
            {147}3{578}{46}{56}2{16}{78}9
            {147}{128}{2578}{346}9{578}{16}{2378}{2358}
            6{289}{25789}1{3578}{578}{235}4{2358}
            {17}{18}{378}546{239}{2389}{238}
            256{389}{378}{789}4{38}1
            94{38}2{38}1756
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 4, column: 2, candidates: 8),
            BoardModification(row: 4, column: 7, candidates: 8),
            BoardModification(row: 5, column: 2, candidates: 8),
            BoardModification(row: 6, column: 2, candidates: 8),
            BoardModification(row: 6, column: 7, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule1)
    }
    
    func testRule2Test1() {
        let board = """
            {123}8{249}5{12}7{234}6{12349}
            7{36}{25}94{126}{235}{1235}8
            {125}{2456}{2459}38{26}7{259}{1249}
            {56}7{456}{246}981{23}{235}
            {26}18{26}53947
            9{245}3{124}7{12}68{25}
            8{23}1765{234}{239}{2349}
            4{25}7{12}398{125}6
            {356}9{26}8{12}4{235}7{123}
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 4, column: 0, value: 2)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule2)
    }
    
    func testRule2Test2() {
        let board = """
            2{168}4{16}{36}79{38}5
            {168}9{178}5{136}2{167}4{38}
            {167}354982{167}{16}
            {138}{178}6{78}295{13}4
            {135}{1578}2{78}4{16}{67}9{136}
            {17}4935{16}8{167}2
            {1568}{156}3974{16}2{168}
            92{18}{16}{168}3457
            4{1678}{178}2{18}53{168}9
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 7, value: 8),
            BoardModification(row: 6, column: 8, value: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule2)
    }
    
    func testRule3Test1() {
        let board = """
            {128}{124}37{89}65{2489}{49}
            7{248}{48}5{2389}{38}6{2489}1
            569{128}4{18}{38}7{238}
            {1368}{148}2{489}{137}{348}{38}5{3679}
            {38}956{378}241{378}
            {1368}7{48}{489}{138}52{689}{3689}
            9{28}6{14}5{14}73{28}
            437{28}{268}91{68}5
            {28}513{68}79{2468}{2468}
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 2, column: 3, candidates: 8)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule3)
    }
    
    func testRule3Test2() {
        let board = """
            185{49}2637{49}
            {234}6{234}{3579}{134}{1357}{2458}{28}{59}
            {234}97{35}{34}81{26}{256}
            {4678}1{48}{38}52{68}9{37}
            {2579}{27}{29}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {3789}{27}{2389}{357}{1348}{1357}{2468}{12368}{12368}
            {38}5629{134}7{138}{1348}
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 1, column: 6, candidates: 2)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule3)
    }
    
    func testRule3Test3() {
        let board = """
            185{49}2637{49}
            {234}6{23}{3579}{134}{1357}{458}{28}{59}
            {234}97{35}{34}81{26}{256}
            {678}14{38}52{68}9{37}
            {2579}{27}{29}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {3789}{27}{2389}{357}{1348}{1357}{2468}{12368}{12368}
            {38}5629{134}7{138}{1348}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 7, column: 6, candidates: 8),
            BoardModification(row: 7, column: 7, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule3)
    }
    
    func testRule3Test4() {
        let board = """
            185{49}2637{49}
            {234}6{23}{3579}{134}{1357}{458}{28}{59}
            {234}97{35}{34}81{26}{256}
            {678}14{38}52{68}9{37}
            {2579}{27}{29}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {3789}{27}{2389}{357}{1348}{1357}{246}{12368}{12368}
            {38}5629{134}7{138}{1348}
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 7, column: 7, candidates: 8)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule3)
    }
    
    func testRule3Test5() {
        let board = """
            1{278}5{37}{238}946{278}
            3496{28}{278}{2578}1{2578}
            {268}{278}{268}1453{289}{2789}
            {248}9{248}{58}1{468}{2568}73
            56{238}{37}9{78}14{28}
            71{348}2{3568}{46}{568}{589}{5689}
            {2468}5{2468}971{268}3{2468}
            {2468}{28}7{458}{2568}39{258}1
            931{458}{2568}{268}{25678}{258}{245678}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 0, candidates: 2),
            BoardModification(row: 2, column: 2, candidates: 2),
            BoardModification(row: 7, column: 1, candidates: 2)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule3)
    }
    
    func testRule3Test6() {
        let board = """
            62{489}{48}53{489}71
            31{489}{478}{24789}{249}{2489}{56}{56}
            {45}{4589}71{2489}63{249}{48}
            {2457}{4589}{2489}3{489}16{2459}{4578}
            {247}{3489}{23489}{4568}{4689}{4589}{24789}1{3478}
            1{34589}62{489}7{489}{459}{3458}
            {24}{34}19{23478}{248}5{46}{467}
            87{245}{56}{1246}{245}{14}39
            96{345}{457}{1347}{45}{147}82
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 4, candidates: 4),
            BoardModification(row: 3, column: 2, candidates: 4),
            BoardModification(row: 4, column: 2, candidates: 4),
            BoardModification(row: 4, column: 6, candidates: 4),
            BoardModification(row: 5, column: 6, candidates: 4),
            BoardModification(row: 6, column: 4, candidates: 4),
            BoardModification(row: 6, column: 5, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: groupedXCyclesRule3)
    }
}
