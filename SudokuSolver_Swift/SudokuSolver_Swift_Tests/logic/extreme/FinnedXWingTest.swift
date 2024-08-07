import XCTest

final class FinnedXWingTest: XCTestCase {
    func test1() {
        let board = """
            754{126}{269}8{12}3{1269}
            836{14}{279}{12479}{57}{279}{12579}
            19235{67}84{67}
            2459{68}{16}{37}{78}{137}
            {36}{68}{138}745{12}{289}{129}
            9{78}{178}{12}{28}3654
            {3456}2981{467}{3457}{67}{357}
            {3456}{678}{378}{246}{2367}{2467}91{2357}
            {346}1{37}5{23679}{24679}{347}{267}8
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 7, column: 8, candidates: 7)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: finnedXWing)
    }
    
    func test2() {
        let board = """
            9{156}{1256}{25}4{1567}38{267}
            7{136}4{23}8{136}95{26}
            {235}8{2356}9{27}{3567}14{267}
            {135}{135}769{35}824
            629418735
            {345}{345}8{235}{27}{357}619
            {345}7{35}162{45}98
            89{25}734{25}61
            {124}{146}{126}859{24}73
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 5, column: 0, candidates: 3, 5)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: finnedXWing)
    }
    
    func test3() {
        let board = """
            9{156}{1256}{25}4{1567}38{267}
            7{136}4{23}8{136}95{26}
            {235}8{2356}9{27}{3567}14{267}
            {135}{135}769{35}824
            629418735
            {45}{345}8{235}{27}{357}619
            {345}7{35}162{45}98
            89{25}734{25}61
            {124}{146}{126}859{24}73
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 5, column: 0, candidates: 5)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: finnedXWing)
    }
    
    func testSashimi() {
        let board = """
            3{467}{67}{46}12598
            {249}{245}1{49}8{459}763
            {69}8{56}7{356}{369}241
            7{269}{256}{469}{456}138{246}
            {246}{24569}387{4569}{469}1{246}
            1{469}82{346}{3469}{469}75
            5193{46}8{46}27
            {26}3{267}19{467}85{46}
            8{67}452{67}139
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 4, column: 5, candidates: 4),
            BoardModification(row: 5, column: 5, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: finnedXWing)
    }
}
