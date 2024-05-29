import XCTest

final class JellyfishTest: XCTestCase {
    func test1() {
        let board = """
            {249}{469}17538{2469}{269}
            {23489}5{23489}{12}{14}{246}{236}{2469}7
            7{346}{234}89{246}1{2456}{2356}
            {3489}{3489}{3489}6{23}157{28}
            625478931
            {38}179{23}54{268}{268}
            {123589}{389}{2389}{125}67{23}{2589}4
            {234589}7{23489}{25}{48}{24}{236}1{235689}
            {12458}{48}63{148}97{258}{258}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 0, candidates: 2),
            BoardModification(row: 1, column: 7, candidates: 2),
            BoardModification(row: 2, column: 7, candidates: 2),
            BoardModification(row: 2, column: 8, candidates: 2),
            BoardModification(row: 6, column: 0, candidates: 2),
            BoardModification(row: 6, column: 7, candidates: 2),
            BoardModification(row: 7, column: 0, candidates: 2),
            BoardModification(row: 7, column: 8, candidates: 2)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
    
    func test2() {
        let board = """
            {234568}{2368}{368}{46789}{178}{4689}{178}{58}{4578}
            {4568}7{68}{468}3{1468}92{1458}
            {48}19{478}2563{478}
            {3689}{368}4{5678}{578}{368}21{35789}
            {23689}{2368}{1368}{24568}{1578}{23468}{78}{58}{35789}
            {1238}57{28}9{1238}46{38}
            {68}9514{28}37{26}
            7{368}{1368}{2589}{58}{289}{18}4{26}
            {18}4236759{18}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 0, candidates: 8),
            BoardModification(row: 0, column: 2, candidates: 8),
            BoardModification(row: 0, column: 3, candidates: 8),
            BoardModification(row: 0, column: 5, candidates: 8),
            BoardModification(row: 0, column: 8, candidates: 8),
            BoardModification(row: 1, column: 0, candidates: 8),
            BoardModification(row: 1, column: 3, candidates: 8),
            BoardModification(row: 1, column: 5, candidates: 8),
            BoardModification(row: 1, column: 8, candidates: 8),
            BoardModification(row: 3, column: 0, candidates: 8),
            BoardModification(row: 3, column: 3, candidates: 8),
            BoardModification(row: 3, column: 5, candidates: 8),
            BoardModification(row: 3, column: 8, candidates: 8),
            BoardModification(row: 4, column: 0, candidates: 8),
            BoardModification(row: 4, column: 2, candidates: 8),
            BoardModification(row: 4, column: 3, candidates: 8),
            BoardModification(row: 4, column: 5, candidates: 8),
            BoardModification(row: 4, column: 8, candidates: 8),
            BoardModification(row: 7, column: 2, candidates: 8),
            BoardModification(row: 7, column: 3, candidates: 8),
            BoardModification(row: 7, column: 5, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
    
    func test3() {
        let board = """
            {123}5{123}749{126}8{126}
            {124}89{56}{256}3{47}{257}{1245}
            6{27}{247}{58}{258}139{245}
            {2389}4{2358}{13589}{3589}7{125}6{1235}
            {1237}{237}{123567}4{356}{26}8{257}9
            {23789}{2379}{235678}{135689}{35689}{268}{47}{257}{12345}
            {2789}6{278}{389}{3789}4{259}1{258}
            5{39}{38}21{68}{69}47
            {24789}1{2478}{689}{6789}5{269}3{268}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 0, candidates: 2),
            BoardModification(row: 1, column: 8, candidates: 2),
            BoardModification(row: 2, column: 2, candidates: 2),
            BoardModification(row: 2, column: 8, candidates: 2),
            BoardModification(row: 4, column: 0, candidates: 2),
            BoardModification(row: 4, column: 2, candidates: 2),
            BoardModification(row: 5, column: 0, candidates: 2),
            BoardModification(row: 5, column: 2, candidates: 2),
            BoardModification(row: 5, column: 8, candidates: 2)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
    
    func test4() {
        let board = """
            {245689}{259}{69}{13456789}{3689}{456789}{19}{15679}{79}
            {4569}17{4569}2{4569}8{569}3
            {5689}{569}3{156789}{689}{56789}2{15679}4
            {19}84{29}537{129}6
            {1356}{56}{169}{246789}{689}{246789}{1349}{123489}{89}
            {369}72{4689}1{4689}{349}{3489}5
            {69}48{369}715{39}2
            {279}35{289}4{289}6{789}1
            {12679}{269}{169}{235689}{3689}{25689}{349}{34789}{789}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 0, candidates: 9),
            BoardModification(row: 0, column: 3, candidates: 9),
            BoardModification(row: 0, column: 5, candidates: 9),
            BoardModification(row: 0, column: 6, candidates: 9),
            BoardModification(row: 0, column: 7, candidates: 9),
            BoardModification(row: 2, column: 0, candidates: 9),
            BoardModification(row: 2, column: 3, candidates: 9),
            BoardModification(row: 2, column: 5, candidates: 9),
            BoardModification(row: 2, column: 7, candidates: 9),
            BoardModification(row: 4, column: 3, candidates: 9),
            BoardModification(row: 4, column: 5, candidates: 9),
            BoardModification(row: 4, column: 6, candidates: 9),
            BoardModification(row: 4, column: 7, candidates: 9),
            BoardModification(row: 5, column: 0, candidates: 9),
            BoardModification(row: 5, column: 3, candidates: 9),
            BoardModification(row: 5, column: 5, candidates: 9),
            BoardModification(row: 5, column: 7, candidates: 9),
            BoardModification(row: 8, column: 0, candidates: 9),
            BoardModification(row: 8, column: 3, candidates: 9),
            BoardModification(row: 8, column: 5, candidates: 9),
            BoardModification(row: 8, column: 6, candidates: 9),
            BoardModification(row: 8, column: 7, candidates: 9)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
    
    func test5() {
        let board = """
            {3589}4{589}{289}7{268}{2569}1{2359}
            {1789}{19}235{168}{4679}{469}{79}
            {13579}6{1579}{249}{24}{12}{2579}8{23579}
            2{159}{14589}{46}37{14569}{4569}{1589}
            {58}3{458}1{468}9{4567}2{578}
            67{1489}{245}{248}{25}3{49}{189}
            4{15}3{258}9{258}{128}76
            {159}8{1569}7{26}4{1259}3{1259}
            {79}2{679}{568}13{589}{59}4
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 3, column: 2, candidates: 5),
            BoardModification(row: 3, column: 6, candidates: 5),
            BoardModification(row: 3, column: 8, candidates: 5),
            BoardModification(row: 8, column: 6, candidates: 5)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
    
    func test6() {
        let board = """
            {245}{24567}{456}{18}{59}{18}{23457}{4567}{3679}
            8{567}3{59}24{57}1{679}
            9{245}1376{245}8{245}
            6{145}7{145}8392{45}
            {2345}{23458}{45}{24567}{456}91{4567}{3678}
            {2345}{1234589}{459}{124567}{456}{1257}{34578}{4567}{3678}
            7{69}8{69}1{25}{245}3{245}
            {345}{34569}{4569}{2456789}{4569}{2578}{2578}{57}1
            1{45}2{4578}3{578}69{78}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 1, candidates: 4, 5),
            BoardModification(row: 0, column: 6, candidates: 4, 5),
            BoardModification(row: 4, column: 1, candidates: 4, 5),
            BoardModification(row: 4, column: 3, candidates: 4, 5),
            BoardModification(row: 5, column: 1, candidates: 4, 5),
            BoardModification(row: 5, column: 3, candidates: 4, 5),
            BoardModification(row: 5, column: 5, candidates: 5),
            BoardModification(row: 5, column: 6, candidates: 4, 5),
            BoardModification(row: 7, column: 1, candidates: 4, 5),
            BoardModification(row: 7, column: 3, candidates: 4, 5),
            BoardModification(row: 7, column: 5, candidates: 5),
            BoardModification(row: 7, column: 6, candidates: 5)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
    
    func test7() {
        let board = """
            14{2358}{2568}{38}{256}{2358}97
            97{2358}{258}{348}{245}{2358}16
            {2358}{58}6{79}{19}{179}{23458}{2458}{2458}
            {28}9145376{28}
            {25}6{245}1789{245}3
            73{458}{69}2{69}1{458}{458}
            {358}{58}{35789}{235789}{19}{124579}6{2458}{2458}
            42{3589}{3589}6{59}{58}71
            61{578}{2578}{48}{2457}{2458}39
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 6, candidates: 5, 8),
            BoardModification(row: 4, column: 2, candidates: 5),
            BoardModification(row: 5, column: 2, candidates: 5, 8),
            BoardModification(row: 6, column: 2, candidates: 5, 8),
            BoardModification(row: 6, column: 3, candidates: 5, 8),
            BoardModification(row: 6, column: 5, candidates: 5)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
    
    func test8() {
        let board = """
            {1245678}{145689}{2678}{15789}{1578}{89}{348}{23478}{278}
            {1578}{158}3{1578}246{78}9
            {24789}{489}{278}{789}365{2478}1
            {138}24{38}679{18}5
            {13689}{13689}{68}{23589}{58}{289}{48}{14678}{678}
            {689}75{89}412{68}3
            {268}{68}1{268}93754
            {234678}{3468}9{24678}{78}51{2368}{268}
            {2345678}{34568}{2678}{124678}{178}{28}{38}9{268}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 0, candidates: 8),
            BoardModification(row: 0, column: 1, candidates: 8),
            BoardModification(row: 0, column: 2, candidates: 8),
            BoardModification(row: 0, column: 3, candidates: 8),
            BoardModification(row: 0, column: 7, candidates: 8),
            BoardModification(row: 2, column: 0, candidates: 8),
            BoardModification(row: 2, column: 1, candidates: 8),
            BoardModification(row: 2, column: 3, candidates: 8),
            BoardModification(row: 2, column: 7, candidates: 8),
            BoardModification(row: 4, column: 0, candidates: 8),
            BoardModification(row: 4, column: 1, candidates: 8),
            BoardModification(row: 4, column: 2, candidates: 8),
            BoardModification(row: 4, column: 3, candidates: 8),
            BoardModification(row: 4, column: 7, candidates: 8),
            BoardModification(row: 7, column: 0, candidates: 8),
            BoardModification(row: 7, column: 1, candidates: 8),
            BoardModification(row: 7, column: 3, candidates: 8),
            BoardModification(row: 7, column: 7, candidates: 8),
            BoardModification(row: 8, column: 0, candidates: 8),
            BoardModification(row: 8, column: 1, candidates: 8),
            BoardModification(row: 8, column: 2, candidates: 8),
            BoardModification(row: 8, column: 3, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
    
    func test9() {
        let board = """
            {234567}{3457}{27}{156789}{16789}{178}{678}{34678}{2467}
            {24567}{457}9{5678}{678}31{24678}{2467}
            {367}81{67}249{367}5
            {178}64{178}592{78}3
            {15789}{57}{78}{123478}{13478}{1278}{678}{46789}{467}
            {789}23{478}{478}65{4789}1
            {2478}15{24678}{4678}{278}3{267}9
            {23478}{347}{278}{12346789}{1346789}{1278}{67}5{267}
            {237}96{237}{37}5418
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 0, candidates: 7),
            BoardModification(row: 0, column: 3, candidates: 7),
            BoardModification(row: 0, column: 4, candidates: 7),
            BoardModification(row: 0, column: 5, candidates: 7),
            BoardModification(row: 0, column: 7, candidates: 7),
            BoardModification(row: 1, column: 0, candidates: 7),
            BoardModification(row: 1, column: 3, candidates: 7),
            BoardModification(row: 1, column: 4, candidates: 7),
            BoardModification(row: 1, column: 7, candidates: 7),
            BoardModification(row: 4, column: 0, candidates: 7),
            BoardModification(row: 4, column: 3, candidates: 7),
            BoardModification(row: 4, column: 4, candidates: 7),
            BoardModification(row: 4, column: 5, candidates: 7),
            BoardModification(row: 4, column: 7, candidates: 7),
            BoardModification(row: 6, column: 0, candidates: 7),
            BoardModification(row: 6, column: 3, candidates: 7),
            BoardModification(row: 6, column: 4, candidates: 7),
            BoardModification(row: 6, column: 7, candidates: 7),
            BoardModification(row: 7, column: 0, candidates: 7),
            BoardModification(row: 7, column: 3, candidates: 7),
            BoardModification(row: 7, column: 4, candidates: 7),
            BoardModification(row: 7, column: 5, candidates: 7)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: jellyfish)
    }
}
