package sudokusolver.javanostreams.logic.extreme;

import org.junit.jupiter.api.Test;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.logic.SudokuAssertions;

import java.util.List;

class FinnedSwordfishTest {
    @Test
    public void test1() {
        var board = """
                6{379}5418{379}{37}2
                8{349}{349}{3569}72{3459}{3456}1
                21{3479}{3569}{359}{69}{457}8{367}
                9{37}8265{137}{137}4
                {34}5{3467}18{49}2{3679}{367}
                1{246}{246}7{49}38{69}5
                {34}8{169}{3569}{359}{4679}{13457}2{37}
                5{69}{169}{369}2{4679}{1347}{1347}8
                7{234}{234}8{345}16{345}9""".replace("\n", "");
        var expected = List.of(new RemoveCandidates(6, 3, 3));
        SudokuAssertions.assertLogicalSolution(expected, board, FinnedSwordfish::finnedSwordfish);
    }

    @Test
    public void test2() {
        var board = """
                2{349}{3489}{1359}{1358}{1389}6{19}7
                {189}7{89}6{128}4{1259}3{1259}
                {139}65{1239}7{1239}84{129}
                582{139}6{139}{139}74
                4{39}78{123}5{1239}{129}6
                {39}16{2379}4{2379}{239}58
                {378}{35}1{2357}9{2378}46{25}
                {379}2{39}4{135}6{1579}8{159}
                6{459}{489}{1257}{1258}{1278}{12579}{129}3""".replace("\n", "");
        var expected = List.of(new RemoveCandidates(7, 0, 3));
        SudokuAssertions.assertLogicalSolution(expected, board, FinnedSwordfish::finnedSwordfish);
    }

    @Test
    public void test3() {
        var board = """
                42{36}{17}{678}{18}{1367}95
                {3589}{357}{3569}{1257}4{157}{12367}{267}{2378}
                {58}{57}19{2567}34{267}{278}
                {357}6{35}892{57}14
                {57}4231{57}986
                1984{57}6{257}3{27}
                {2359}176{235}48{25}{239}
                {2359}{35}4{1257}{23578}{15789}{267}{2567}{2379}
                68{359}{257}{2357}{579}{237}41""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(1, 5, 7),
                new RemoveCandidates(7, 5, 7)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, FinnedSwordfish::finnedSwordfish);
    }

    @Test
    public void test4() {
        var board = """
                42{36}{17}{678}{18}{1367}95
                {3589}{357}{3569}{1257}4{15}{12367}{267}{2378}
                {58}{57}19{2567}34{267}{278}
                {357}6{35}892{57}14
                {57}4231{57}986
                1984{57}6{257}3{27}
                {2359}176{235}48{25}{239}
                {2359}{35}4{1257}{23578}{15789}{267}{2567}{2379}
                68{359}{257}{2357}{579}{237}41""".replace("\n", "");
        var expected = List.of(new RemoveCandidates(7, 5, 7));
        SudokuAssertions.assertLogicalSolution(expected, board, FinnedSwordfish::finnedSwordfish);
    }

    @Test
    public void test5() {
        var board = """
                {2456}{2567}3{245}8{259}1{456}{79}
                {124568}9{1468}{2345}{1345}7{348}{4568}{3568}
                {1458}{458}{1478}6{39}{15}{79}{3458}2
                {568}1{79}{258}{567}3{289}{268}4
                {4689}{4678}2{478}{1467}{168}5{1368}{39}
                3{4568}{468}9{1456}{12568}{28}7{168}
                7{2368}{19}{358}{3569}4{238}{1258}{1358}
                {2468}{23468}{468}1{3567}{568}{23478}9{3578}
                {19}{348}5{378}2{89}6{148}{1378}""".replace("\n", "");
        var expected = List.of(new RemoveCandidates(5, 4, 6));
        SudokuAssertions.assertLogicalSolution(expected, board, FinnedSwordfish::finnedSwordfish);
    }

    @Test
    public void test6() {
        var board = """
                {256}7348{25}1{56}9
                {26}9{16}{235}{135}74{568}{568}
                {458}{458}{148}69{15}732
                {568}17{258}{56}39{268}4
                9{468}2{78}{1467}{168}5{16}3
                3{4568}{468}9{145}{12568}{28}7{168}
                7{2368}9{358}{356}4{238}{1258}{158}
                {468}{23468}{468}1{3567}{568}{23}9{578}
                1{38}5{378}2964{78}""".replace("\n", "");
        var expected = List.of(new RemoveCandidates(7, 0, 6));
        SudokuAssertions.assertLogicalSolution(expected, board, FinnedSwordfish::finnedSwordfish);
    }
}