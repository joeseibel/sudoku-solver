import XCTest

final class HiddenQuadsTest: XCTestCase {
    func test1() {
        let board = """
            65{139}{13}87{19}24
            {278}{28}{1378}649{18}5{37}
            {89}4{378}{13}25{168}{37}{69}
            57{29}438{29}61
            {2489}{2689}{468}5{67}1{347}{347}{29}
            31{46}9{67}2{47}85
            {247}{26}{457}89{46}{357}1{237}
            {4789}{689}{578}213{4576}{47}{67}
            13{246}75{46}{26}98
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 7, column: 6, candidates: 6)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: hiddenQuads)
    }
    
    func test2() {
        let board = """
            9{37}15{28}{28}{37}46
            425{367}9{367}{37}81
            86{37}{347}1{347}{59}2{59}
            5{3478}2{1346789}{378}{346789}{19}{37}{89}
            {37}19{2378}{23578}{23578}46{58}
            6{3478}{3478}{134789}{3578}{345789}{159}{37}2
            196{78}4{78}253
            2{345}{34}{39}6{359}817
            {37}{3578}{378}{23}{235}1694
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 3, column: 3, candidates: 3, 7, 8),
            BoardModification(row: 3, column: 5, candidates: 3, 7, 8),
            BoardModification(row: 5, column: 3, candidates: 3, 7, 8),
            BoardModification(row: 5, column: 5, candidates: 3, 5, 7, 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: hiddenQuads)
    }
}
