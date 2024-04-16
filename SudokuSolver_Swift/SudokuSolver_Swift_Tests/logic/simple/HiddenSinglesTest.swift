import XCTest

final class HiddenSinglesTest: XCTestCase {
    func test() {
        let board = """
            2{459}{1569}{159}7{159}{159}38
            {458}{4589}{159}{123589}{159}6{1259}7{145}
            3{5789}{159}{12589}4{12589}6{1259}{15}
            {456}{3459}8{1569}2{1459}7{159}{135}
            1{23459}{2359}{5789}{59}{45789}{23589}{2589}6
            {56}{259}7{15689}3{1589}4{12589}{15}
            {57}{2357}4{12357}8{12357}{135}{156}9
            {578}6{235}4{159}{123579}{1358}{158}{1357}
            91{35}{357}6{357}{358}{458}2
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 1, value: 4),
            BoardModification(row: 0, column: 2, value: 6),
            BoardModification(row: 1, column: 3, value: 3),
            BoardModification(row: 1, column: 8, value: 4),
            BoardModification(row: 2, column: 1, value: 7),
            BoardModification(row: 6, column: 7, value: 6),
            BoardModification(row: 7, column: 0, value: 8),
            BoardModification(row: 7, column: 8, value: 7),
            BoardModification(row: 8, column: 7, value: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: hiddenSingles)
    }
}
