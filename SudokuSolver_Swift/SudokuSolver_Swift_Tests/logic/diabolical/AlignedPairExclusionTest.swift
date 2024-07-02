import XCTest

final class AlignedPairExclusionTest: XCTestCase {
    func test1() {
        let board = """
            {568}971{258}3{268}4{258}
            {568}3{128}{258}4{259}7{1269}{2589}
            {458}{124}{128}67{259}{289}{129}3
            273914586
            986{235}{25}71{23}4
            154{23}68{239}{239}7
            7{24}{258}{2458}91{36}{36}{28}
            {48}{124}9736{248}5{12}
            36{1258}{2458}{258}{25}{2489}7{1289}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 6, column: 2, candidates: 8),
            BoardModification(row: 7, column: 6, candidates: 8)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test2() {
        let board = """
            {56}971{258}3{268}4{25}
            {568}3{128}{258}4{259}7{1269}{2589}
            {458}{124}{128}67{259}{289}{129}3
            273914586
            986{235}{25}71{23}4
            154{23}68{239}{239}7
            7{24}{25}{2458}91{36}{36}{28}
            {48}{124}9736{248}5{12}
            36{1258}{2458}{258}{25}{2489}7{1289}
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 7, column: 6, candidates: 8)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test3() {
        let board = """
            {17}{1458}{457}{14}23{69}{69}{78}
            62{37}598{137}{137}4
            {149}{138}{349}7{14}652{38}
            598{13}62{137}4{137}
            2{34}1{349}758{39}6
            {34}768{14}{19}{139}52
            {1349}62{19}{58}7{134}{138}{135}
            8{15}{57}6342{17}9
            {13479}{134}{3479}2{58}{19}{13467}{3678}{1357}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 2, column: 1, candidates: 1),
            BoardModification(row: 2, column: 2, candidates: 3)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test4() {
        let board = """
            {17}{1458}{457}{14}23{69}{69}{78}
            62{37}598{137}{137}4
            {149}{38}{49}7{14}652{38}
            598{13}62{137}4{137}
            2{34}1{349}758{39}6
            {34}768{14}{19}{139}52
            {1349}62{19}{58}7{134}{138}{135}
            8{15}{57}6342{17}9
            {13479}{134}{3479}2{58}{19}{13467}{3678}{157}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 2, candidates: 7),
            BoardModification(row: 6, column: 7, candidates: 1)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test5() {
        let board = """
            {124}{159}{2589}{689}{568}{49}37{15}
            7{1459}6{89}{458}3{128}{458}{1245}
            3{45}{58}172{68}{4568}9
            9672385{14}{14}
            531496728
            824517936
            6{1479}{29}3{248}5{128}{18}{127}
            {12}{157}{25}{6789}{268}{19}4{1689}3
            {124}83{679}{26}{149}{126}{1569}{157}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 0, column: 5, candidates: 9),
            BoardModification(row: 1, column: 8, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test6() {
        let board = """
            {18}24{19}536{1789}{78}
            {168}{13678}5{1789}4{89}{139}{189}2
            9{137}{178}6{78}2{134}{14}5
            7{4689}{2689}5{289}1{249}3{468}
            {148}{1489}{1289}3675{2489}{48}
            35{2689}{89}{289}4{279}{26789}1
            2{4678}{678}{478}35{147}{1467}9
            5{46789}{79}{4789}1{689}{247}{2467}3
            {146}{1469}32{79}{69}85{467}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 0, candidates: 8),
            BoardModification(row: 1, column: 1, candidates: 8),
            BoardModification(row: 1, column: 5, candidates: 8),
            BoardModification(row: 7, column: 5, candidates: 9),
            BoardModification(row: 8, column: 5, candidates: 9)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test7() {
        let board = """
            45{37}9{136}{367}{278}{168}{1268}
            {127}{123}68{134}{3457}9{15}{14}
            {179}8{79}{146}2{4567}{457}3{146}
            {279}{239}{3789}5{3469}{23469}{2348}{1689}{12468}
            64{389}{123}{139}{239}{238}75
            5{239}1{2346}78{234}{69}{246}
            375{26}8{26}149
            {189}{19}2{34}{3459}{349}6{58}7
            {89}647{59}1{58}23
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 0, candidates: 1),
            BoardModification(row: 1, column: 1, candidates: 1),
            BoardModification(row: 2, column: 8, candidates: 1),
            BoardModification(row: 3, column: 8, candidates: 4),
            BoardModification(row: 5, column: 8, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test8() {
        let board = """
            45{37}9{136}{367}{278}{168}{1268}
            {27}{123}68{134}{3457}9{15}{14}
            {179}8{79}{146}2{4567}{457}3{146}
            {279}{239}{3789}5{3469}{23469}{2348}{1689}{12468}
            64{389}{123}{139}{239}{238}75
            5{239}1{2346}78{234}{69}{246}
            375{26}8{26}149
            {189}{19}2{34}{3459}{349}6{58}7
            {89}647{59}1{58}23
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 1, candidates: 1),
            BoardModification(row: 2, column: 8, candidates: 1),
            BoardModification(row: 3, column: 8, candidates: 4),
            BoardModification(row: 5, column: 8, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test9() {
        let board = """
            9{45}{47}{378}{28}{237}1{235}6
            {67}{16}259{1367}{478}{38}{478}
            3{156}8{167}4{1267}{257}9{257}
            {246}8{1346}{369}{16}{3569}{235}7{125}
            57{13}2{18}4{38}69
            {26}9{136}{368}7{356}{2358}4{1258}
            8{24}9{147}5{127}6{12}3
            {467}{246}{467}{14}389{125}{245}
            135{469}{26}{269}{2478}{28}{2478}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 6, candidates: 7),
            BoardModification(row: 1, column: 8, candidates: 7),
            BoardModification(row: 2, column: 3, candidates: 7),
            BoardModification(row: 2, column: 5, candidates: 7),
            BoardModification(row: 3, column: 0, candidates: 6),
            BoardModification(row: 3, column: 2, candidates: 4),
            BoardModification(row: 5, column: 0, candidates: 6)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test10() {
        let board = """
            9{45}{47}{378}{28}{237}1{235}6
            {67}{16}259{1367}{478}{38}{478}
            3{156}8{167}4{127}{257}9{257}
            48{136}{369}{16}{3569}{235}7{125}
            57{13}2{18}4{38}69
            29{136}{368}7{356}{358}4{158}
            8{24}9{147}5{127}6{12}3
            {67}{246}{47}{14}389{125}{245}
            135{469}{26}{269}{2478}{28}{2478}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 6, candidates: 7),
            BoardModification(row: 1, column: 8, candidates: 7),
            BoardModification(row: 2, column: 3, candidates: 7),
            BoardModification(row: 2, column: 5, candidates: 7)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test11() {
        let board = """
            9{45}{47}{378}{28}{237}1{235}6
            {67}{16}259{1367}{478}{38}{478}
            3{156}8{16}4{1267}{257}9{257}
            48{136}{369}{16}{3569}{235}7{125}
            57{13}2{18}4{38}69
            29{136}{368}7{356}{358}4{158}
            8{24}9{147}5{127}6{12}3
            {67}{246}{47}{14}389{125}{245}
            135{469}{26}{269}{2478}{28}{2478}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 6, candidates: 7),
            BoardModification(row: 1, column: 8, candidates: 7),
            BoardModification(row: 2, column: 5, candidates: 7)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test12() {
        let board = """
            {135}72{159}8{35}64{59}
            {1345}967{124}{2345}8{235}{25}
            {345}{35}86{249}{2345}{23579}{23579}1
            {579}{25}{579}3{12679}{26}{129}84
            681{49}{2479}{247}{2359}{2359}{2579}
            {379}{23}4{19}58{129}6{279}
            21{57}8394{57}6
            84{579}2{67}{567}{579}13
            {579}63{45}{47}1{2579}{2579}8
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 1, column: 4, candidates: 4),
            BoardModification(row: 2, column: 4, candidates: 4),
            BoardModification(row: 4, column: 5, candidates: 4)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test13() {
        let board = """
            {135}72{159}8{35}64{59}
            {1345}967{12}{2345}8{235}{25}
            {345}{35}86{29}{2345}{23579}{23579}1
            {579}{25}{579}3{12679}{26}{129}84
            681{49}{2479}{27}{2359}{2359}{2579}
            {379}{23}4{19}58{129}6{279}
            21{57}8394{57}6
            84{579}2{67}{567}{579}13
            {579}63{45}{47}1{2579}{2579}8
            """.replacing("\n", with: "")
        let expected = [BoardModification(row: 4, column: 4, candidates: 2)]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
    
    func test14() {
        let board = """
            185{49}2637{49}
            {234}6{234}{3579}{134}{1357}{2458}{28}{2589}
            {234}97{345}{34}81{26}{2456}
            {4678}1{48}{348}52{68}9{37}
            {245789}{27}{2489}{348}6{34}{258}{13}{137}
            {2568}3{28}179{2568}4{2568}
            {2378}416{38}{37}95{238}
            {23789}{27}{2389}{357}{1348}{13457}{2468}{12368}{123468}
            {38}5629{134}7{138}{1348}
            """.replacing("\n", with: "")
        let expected = [
            BoardModification(row: 4, column: 0, candidates: 4, 8),
            BoardModification(row: 4, column: 2, candidates: 4, 8),
            BoardModification(row: 7, column: 0, candidates: 2)
        ]
        assertLogicalSolution(expected: expected, withCandidates: board, logicFunction: alignedPairExclusion)
    }
}
