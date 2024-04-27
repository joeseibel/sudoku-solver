import XCTest

final class BoxLineReductionTest: XCTestCase {
    func test1() {
        let board = """
            {45}16{245}{2459}78{49}3
            {345}928{3456}{3456}{147}{47}{1457}
            87{35}{345}{3459}126{459}
            {127}48{1257}{12567}{56}3{79}{179}
            65{17}{1347}{1347}9{147}82
            {127}39{1247}{12478}{48}65{147}
            {1357}6{1357}9{1578}{58}{47}2{478}
            {157}8{157}{1457}{1457}2936
            9246{378}{38}51{78}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 6, candidates: 4),
            BoardModification(row: 1, column: 8, candidates: 4),
            BoardModification(row: 2, column: 8, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: boxLineReduction)
    }
    
    func test2() {
        let board = """
            {68}2{68}943715
            9{13}4{1578}{127}{157}6{23}{28}
            75{13}{168}{126}{16}{389}4{289}
            5{1367}{13679}48{1679}{19}{279}{2679}
            2{1678}{16789}{167}{1679}{1679}453
            4{167}{1679}352{189}{79}{6789}
            {36}42{567}{3679}{5679}{39}81
            {138}{1378}5{17}{1379}426{79}
            {136}9{1367}2{1367}85{37}4
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 3, column: 2, candidates: 6),
            BoardModification(row: 3, column: 6, candidates: 9),
            BoardModification(row: 3, column: 8, candidates: 9),
            BoardModification(row: 4, column: 2, candidates: 6),
            BoardModification(row: 5, column: 2, candidates: 6),
            BoardModification(row: 5, column: 6, candidates: 9),
            BoardModification(row: 5, column: 8, candidates: 9),
            BoardModification(row: 7, column: 1, candidates: 1, 3),
            BoardModification(row: 7, column: 3, candidates: 7),
            BoardModification(row: 7, column: 4, candidates: 7),
            BoardModification(row: 8, column: 2, candidates: 1, 3),
            BoardModification(row: 8, column: 4, candidates: 7)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: boxLineReduction)
    }
}
