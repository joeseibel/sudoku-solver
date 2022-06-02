package sudokusolver.javanostreams.logic.tough;

import org.junit.jupiter.api.Test;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.logic.SudokuAssertions;

import java.util.List;

class XWingTest {
    @Test
    public void test1() {
        var board = """
                1{378}{37}{23478}{278}{23478}569
                492{37}561{37}8
                {378}561{78}924{37}
                {357}{37}964{27}8{25}1
                {57}64{2789}1{278}{379}{25}{37}
                218{79}356{79}4
                {378}4{37}5{2789}{2378}{379}16
                9{378}5{378}614{378}2
                621{3478}{789}{3478}{379}{3789}5""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(0, 3, 7),
                new RemoveCandidates(4, 3, 7),
                new RemoveCandidates(7, 3, 7),
                new RemoveCandidates(7, 7, 7),
                new RemoveCandidates(8, 3, 7),
                new RemoveCandidates(8, 7, 7)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, XWing::xWing);
    }

    @Test
    public void test2() {
        var board = """
                {1358}{235}{12358}{3568}{678}{35678}{67}94
                76{48}91{48}{23}5{23}
                {345}9{345}{3456}{467}2{67}81
                {346}7{23469}{2468}5{468}{23489}1{2389}
                {13456}{235}{123456}7{2468}9{23458}{23}{238}
                {45}8{2459}{24}31{2459}67
                24{3568}1{68}{3568}{389}7{3689}
                {368}1{3678}{2368}9{3678}{238}45
                9{35}{35678}{234568}{24678}{345678}1{23}{2368}""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(4, 1, 2),
                new RemoveCandidates(4, 2, 2),
                new RemoveCandidates(4, 6, 2),
                new RemoveCandidates(4, 8, 2),
                new RemoveCandidates(8, 3, 2),
                new RemoveCandidates(8, 8, 2)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, XWing::xWing);
    }

    @Test
    public void test3() {
        var board = """
                {24568}{2458}391{568}7{58}{258}
                {568}{578}{67}{568}23491
                1{258}9{58}47{238}{358}6
                {4589}617{35}{48}{238}{3458}{24589}
                {458}{34578}21{35}96{34578}{4578}
                {4589}{34578}{47}{48}62{38}1{45789}
                79{46}{456}8{456}123
                31829{46}5{467}{47}
                {246}{24}53719{468}{48}""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(1, 0, 6),
                new RemoveCandidates(5, 0, 4),
                new RemoveCandidates(5, 1, 4),
                new RemoveCandidates(5, 8, 4),
                new RemoveCandidates(6, 5, 4, 6)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, XWing::xWing);
    }

    @Test
    public void test4() {
        var board = """
                {2589}1{258}{69}37{58}4{56}
                {4789}{38}{478}{69}25{378}1{67}
                6{35}{57}418{357}29
                {25}7314968{25}
                1{256}{245}87{26}{245}93
                {248}{268}935{26}{124}7{14}
                39{17}264{17}58
                {27}4658193{27}
                {258}{258}{1258}793{124}6{14}""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(0, 0, 2),
                new RemoveCandidates(1, 2, 7),
                new RemoveCandidates(1, 6, 7),
                new RemoveCandidates(5, 0, 2),
                new RemoveCandidates(8, 0, 2)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, XWing::xWing);
    }
}