import XCTest

final class NakedQuadsTest: XCTestCase {
    func test() {
        let board = """
            {15}{1245}{2457}{45}3{19}{79}86
            {1568}{1568}{35678}{56}2{19}{79}4{13}
            {16}9{346}{46}7852{13}
            371856294
            9{68}{68}142375
            4{25}{25}397618
            2{146}{46}7{16}3859
            {18}392{18}5467
            7{568}{568}9{68}4132
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 1, candidates: 1, 5),
            BoardModification(row: 0, column: 2, candidates: 5),
            BoardModification(row: 1, column: 2, candidates: 5, 6, 8),
            BoardModification(row: 2, column: 2, candidates: 6)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: nakedQuads)
    }
}
