import XCTest

final class HiddenTriplesTest: XCTestCase {
    func test() {
        let board = """
            {4789}{489}{47}{245678}{478}1{2469}3{245789}
            231{45678}9{578}{46}{56}{4578}
            {4789}65{2478}{478}31{289}{24789}
            6789243{15}{15}
            1{249}3{78}5{78}{249}{29}6
            {459}{2459}{24}1367{289}{2489}
            {48}{1248}936{28}57{12}
            {57}{25}6{257}19843
            3{12458}{247}{24578}{478}{2578}{269}{16}{129}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 3, candidates: 4, 7, 8),
            BoardModification(row: 0, column: 6, candidates: 4, 9),
            BoardModification(row: 0, column: 8, candidates: 4, 7, 8, 9)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: hiddenTriples)
    }
}
