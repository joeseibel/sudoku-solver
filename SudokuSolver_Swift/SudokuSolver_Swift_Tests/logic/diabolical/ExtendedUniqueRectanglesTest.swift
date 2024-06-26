import XCTest

final class ExtendedUniqueRectanglesTest: XCTestCase {
    func test1() {
        let board = """
            9{16}7{18}243{68}5
            842365917
            {156}3{15}9{18}74{268}{28}
            {125}{1578}4{57}{138}96{258}{238}
            {135}{178}{13}246{17}{58}9
            {256}{1567}9{57}{138}{18}{17}4{238}
            {135}{15}{135}492876
            796{18}5{18}234
            428673591
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 2, column: 0, candidates: 1, 5)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: extendedUniqueRectangles)
    }
    
    func test2() {
        let board = """
            {45}6382{145}{79}{79}{145}
            7{48}{58}{1345}{135}92{14}6
            219{456}{56}738{45}
            {456}32{14567}9{1456}8{1467}{14}
            9{478}{58}{134567}{13568}{13456}{47}{1467}2
            {46}{478}1{467}{68}2539
            124978653
            8562{13}{13}{49}{49}7
            397{56}4{56}128
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 4, column: 7, candidates: 4, 7)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: extendedUniqueRectangles)
    }
    
    func test3() {
        let board = """
            {367}9{347}8{46}152{37}
            851{37}926{37}4
            {367}2{347}{37}{46}5918
            {1237}{138}965{37}{378}4{237}
            4{38}{25}{129}{12}{37}{378}{59}6
            {2357}6{2357}{29}841{3579}{2379}
            {2359}7{235}{12}{123}648{139}
            {139}{13}64782{39}5
            {23}485{123}9{37}6{137}
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 3, column: 1, candidates: 3, 8)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: extendedUniqueRectangles)
    }
}
