import XCTest

final class PointingPairsPointingTriplesTest: XCTestCase {
    func test1() {
        let board = """
            {2458}179{245}36{48}{248}
            {23456}{2345}{36}{1257}8{57}{139}{149}{12349}
            9{2348}{368}{12}{246}{46}5{148}7
            {58}72{58}1{69}43{69}
            {1358}{3589}{389}4{569}2{189}7{1689}
            {18}6437{89}25{189}
            7{23489}1{28}{249}{489}{389}65
            {2468}{2489}{689}{57}3{57}{189}{1489}{1489}
            {348}{3489}56{49}172{3489}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 0, candidates: 3),
            BoardModification(row: 1, column: 1, candidates: 3),
            BoardModification(row: 1, column: 2, candidates: 3),
            BoardModification(row: 2, column: 2, candidates: 6),
            BoardModification(row: 4, column: 4, candidates: 9),
            BoardModification(row: 4, column: 6, candidates: 9),
            BoardModification(row: 4, column: 8, candidates: 9),
            BoardModification(row: 6, column: 1, candidates: 2, 8),
            BoardModification(row: 6, column: 6, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: pointingPairsPointingTriples)
    }
    
    func test2() {
        let board = """
            {789}32{478}{4578}61{4589}{78}
            41{5689}{2378}{3578}{2357}{23679}{23589}{23678}
            {678}{78}{568}9{34578}1{23467}{23458}{23678}
            5{278}{18}{16}9{37}{236}{238}4
            {289}6{489}{348}{3458}{345}{239}71
            3{4789}{1489}{16}2{47}{69}{89}5
            {1269}{249}{13469}5{13467}8{2347}{234}{237}
            {268}{248}{3468}{2347}{3467}{2347}519
            {12}57{234}{134}986{23}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 7, candidates: 8),
            BoardModification(row: 1, column: 5, candidates: 7),
            BoardModification(row: 1, column: 6, candidates: 2, 6),
            BoardModification(row: 1, column: 7, candidates: 2, 8),
            BoardModification(row: 1, column: 8, candidates: 2),
            BoardModification(row: 2, column: 1, candidates: 7),
            BoardModification(row: 2, column: 6, candidates: 6),
            BoardModification(row: 2, column: 7, candidates: 8),
            BoardModification(row: 4, column: 0, candidates: 8),
            BoardModification(row: 4, column: 2, candidates: 8),
            BoardModification(row: 6, column: 1, candidates: 4),
            BoardModification(row: 6, column: 2, candidates: 1, 4),
            BoardModification(row: 6, column: 4, candidates: 4, 7),
            BoardModification(row: 7, column: 5, candidates: 7)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: pointingPairsPointingTriples)
    }
    
    func test3() {
        let board = """
            93{147}{47}5{18}{24678}{1246}{1267}
            2{147}{147}63{18}{478}95
            856{479}{479}2{347}{134}{137}
            {46}{29}318{469}57{26}
            {1467}{1467}5{347}2{3467}98{136}
            {1467}8{29}{3479}{479}5{2346}{12346}{1236}
            {3467}{2467}{247}8{47}{347}159
            5{679}821{379}{367}{36}4
            {1347}{12479}{12479}56{3479}{237}{23}8
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 6, candidates: 7),
            BoardModification(row: 1, column: 6, candidates: 7),
            BoardModification(row: 2, column: 6, candidates: 7),
            BoardModification(row: 3, column: 5, candidates: 9),
            BoardModification(row: 4, column: 5, candidates: 3),
            BoardModification(row: 5, column: 0, candidates: 4),
            BoardModification(row: 5, column: 3, candidates: 4),
            BoardModification(row: 5, column: 4, candidates: 4),
            BoardModification(row: 7, column: 1, candidates: 6),
            BoardModification(row: 8, column: 1, candidates: 2),
            BoardModification(row: 8, column: 2, candidates: 2)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: pointingPairsPointingTriples)
    }
}
