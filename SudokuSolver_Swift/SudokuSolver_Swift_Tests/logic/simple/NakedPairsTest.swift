import XCTest

final class NakedPairsTest: XCTestCase {
    func test1() {
        let board = """
            4{16}{16}{125}{12567}{2567}938
            {78}32{58}941{56}{567}
            {178}953{1678}{67}24{67}
            37{18}6{258}9{58}{1258}4
            529{48}{48}1673
            6{18}47{258}3{58}9{125}
            957{124}{1246}83{126}{126}
            {18}{168}39{12567}{2567}4{12568}{1256}
            24{168}{15}3{56}7{1568}9
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 3, candidates: 1),
            BoardModification(row: 0, column: 4, candidates: 1, 6),
            BoardModification(row: 0, column: 5, candidates: 6),
            BoardModification(row: 2, column: 0, candidates: 1, 7),
            BoardModification(row: 2, column: 4, candidates: 6, 7),
            BoardModification(row: 3, column: 4, candidates: 8),
            BoardModification(row: 3, column: 7, candidates: 5, 8),
            BoardModification(row: 5, column: 4, candidates: 8),
            BoardModification(row: 5, column: 8, candidates: 5)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: nakedPairs)
    }
    
    func test2() {
        let board = """
            {1467}8{567}{12457}9{12}{247}3{24}
            {147}3{57}{12457}{1278}{128}{247}69
            9{47}2{47}63158
            {67}2{67}8{13}459{13}
            8519{23}7{23}46
            3946{12}587{12}
            563{12}4{12}987
            2{47}{789}{37}{378}{689}{346}15
            {47}1{789}{37}5{689}{346}2{34}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 3, candidates: 7),
            BoardModification(row: 1, column: 3, candidates: 7),
            BoardModification(row: 1, column: 5, candidates: 1, 2),
            BoardModification(row: 2, column: 3, candidates: 7),
            BoardModification(row: 7, column: 2, candidates: 7),
            BoardModification(row: 7, column: 4, candidates: 3, 7),
            BoardModification(row: 8, column: 2, candidates: 7)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: nakedPairs)
    }
}
