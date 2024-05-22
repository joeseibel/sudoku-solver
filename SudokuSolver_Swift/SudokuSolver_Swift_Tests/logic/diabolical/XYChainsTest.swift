import SwiftGraph
import XCTest

final class XYChainsTest: XCTestCase {
    func testToDOT() {
        let graph = WeightedUniqueElementsGraph<CodableLocatedCandidate, Strength>()
        let cells = [Cell(row: 0, column: 0), Cell(row: 0, column: 0), Cell(row: 0, column: 4)].unsolvedCells
        let a = CodableLocatedCandidate(cell: cells[0], candidate: .two)
        let b = CodableLocatedCandidate(cell: cells[1], candidate: .six)
        let c = CodableLocatedCandidate(cell: cells[2], candidate: .two)
        let aIndex = graph.addVertex(a)
        let bIndex = graph.addVertex(b)
        let cIndex = graph.addVertex(c)
        graph.addEdge(fromIndex: aIndex, toIndex: bIndex, weight: .strong)
        graph.addEdge(fromIndex: aIndex, toIndex: cIndex, weight: .weak)
        let actual = graph.toDOT()
        let expected = """
            strict graph {
              "[0,0] : 2" -- "[0,0] : 6"
              "[0,0] : 2" -- "[0,4] : 2" [style = dashed]
            }
            """
        XCTAssertEqual(expected, actual)
    }
    
    func test1() {
        let board = """
            {26}8{245}1{29}3{59}7{456}
            {37}9{24}5{27}6{18}{14}{348}
            {37}{56}14{79}8{359}2{356}
            578241639
            143659782
            926837451
            {68}379{16}52{14}{48}
            {268}{56}{25}3{16}4{18}97
            419782{35}6{35}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 2, candidates: 2, 5),
            BoardModification(row: 1, column: 8, candidates: 4),
            BoardModification(row: 2, column: 6, candidates: 5),
            BoardModification(row: 2, column: 8, candidates: 5),
            BoardModification(row: 7, column: 0, candidates: 6)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: xyChains)
    }
    
    func test2() {
        let board = """
            {48}92{145}{18}{158}376
            {478}1{68}{24679}3{2689}5{28}{248}
            3{567}{568}{2467}{2678}{268}19{248}
            93{46}85{26}7{24}1
            {78}{567}{1568}3{126}4{689}{258}{289}
            2{56}{14568}{16}97{68}{458}3
            689{257}{27}341{57}
            523{179}4{189}{89}6{789}
            147{569}{68}{5689}23{589}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 0, candidates: 8),
            BoardModification(row: 1, column: 5, candidates: 8),
            BoardModification(row: 1, column: 8, candidates: 8),
            BoardModification(row: 2, column: 2, candidates: 6),
            BoardModification(row: 4, column: 2, candidates: 6),
            BoardModification(row: 4, column: 7, candidates: 2),
            BoardModification(row: 5, column: 2, candidates: 6)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: xyChains)
    }
    
    func test3() {
        let board = """
            931672458
            672854193
            {58}4{58}913762
            {28}{169}{48}5{349}7{369}{128}{49}
            3{69}{45}{12}{49}8{569}{12}7
            {258}{19}7{12}{349}6{359}{128}{459}
            486321{59}7{59}
            153789246
            729465831
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 3, column: 4, candidates: 9),
            BoardModification(row: 4, column: 6, candidates: 9)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: xyChains)
    }
    
    func test4() {
        let board = """
            {45}938{24}716{25}
            286591437
            {145}7{14}6{234}{34}89{25}
            {479}{13}{47}2{37}5{69}8{169}
            {89}{13}546{38}27{19}
            {78}621{78}9543
            32{17}9{148}{48}{67}5{46}
            {17}583{14}6{79}2{49}
            649752318
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 0, candidates: 4),
            BoardModification(row: 2, column: 4, candidates: 3, 4),
            BoardModification(row: 2, column: 5, candidates: 4),
            BoardModification(row: 3, column: 0, candidates: 7, 9),
            BoardModification(row: 3, column: 1, candidates: 3),
            BoardModification(row: 3, column: 4, candidates: 7),
            BoardModification(row: 3, column: 6, candidates: 9),
            BoardModification(row: 3, column: 8, candidates: 1, 6),
            BoardModification(row: 4, column: 0, candidates: 8),
            BoardModification(row: 4, column: 1, candidates: 1),
            BoardModification(row: 4, column: 5, candidates: 3),
            BoardModification(row: 4, column: 8, candidates: 9),
            BoardModification(row: 5, column: 0, candidates: 7),
            BoardModification(row: 5, column: 4, candidates: 8),
            BoardModification(row: 6, column: 2, candidates: 7),
            BoardModification(row: 6, column: 4, candidates: 1, 4),
            BoardModification(row: 6, column: 6, candidates: 6),
            BoardModification(row: 6, column: 8, candidates: 4),
            BoardModification(row: 7, column: 0, candidates: 1),
            BoardModification(row: 7, column: 4, candidates: 4),
            BoardModification(row: 7, column: 6, candidates: 7),
            BoardModification(row: 7, column: 8, candidates: 9)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: xyChains)
    }
    
    func test5() {
        let board = """
            9{246}3{458}{267}1{478}{245}{2578}
            8{246}{46}{345}{2367}{56}{3479}{1245}{12579}
            751{348}{23}9{348}6{28}
            187{35}{36}{56}294
            {35}{34}{45}792186
            2{69}{69}148573
            67{58}913{48}{245}{258}
            {35}{39}2684{79}{15}{1579}
            41{89}25763{89}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 8, candidates: 8),
            BoardModification(row: 1, column: 1, candidates: 6),
            BoardModification(row: 1, column: 4, candidates: 3, 6),
            BoardModification(row: 6, column: 8, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: xyChains)
    }
}
