package sudokusolver.javanostreams.logic.diabolical;

import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.Strength;
import sudokusolver.javanostreams.StrengthEdge;
import sudokusolver.javanostreams.SudokuNumber;
import sudokusolver.javanostreams.UnsolvedCell;
import sudokusolver.javanostreams.logic.SudokuAssertions;

import java.util.List;

class XCyclesTest {
    @Test
    public void testToDOT() {
        var a = new UnsolvedCell(0, 0);
        var b = new UnsolvedCell(0, 3);
        var c = new UnsolvedCell(2, 5);
        var builder = new GraphBuilder<>(new SimpleGraph<UnsolvedCell, StrengthEdge>(StrengthEdge.class));
        var graph = builder.addEdge(a, b, new StrengthEdge(Strength.STRONG))
                .addEdge(b, c, new StrengthEdge(Strength.WEAK))
                .build();
        var actual = XCycles.toDOT(graph, SudokuNumber.ONE);
        var expected = """
                strict graph 1 {
                  1 [ label="[0,0]" ];
                  2 [ label="[0,3]" ];
                  3 [ label="[2,5]" ];
                  1 -- 2;
                  2 -- 3 [ style="dashed" ];
                }
                """;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void rule1() {
        var board = """
                {59}241{35}{58}67{389}
                {59}6{38}{238}7{258}41{389}
                7{18}{138}964{58}2{358}
                246591387
                135487296
                879623154
                4{18}{128}{38}{35}976{258}
                35{28}71694{28}
                697{28}4{258}{58}31""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(2, 2, 8),
                new RemoveCandidates(2, 8, 8),
                new RemoveCandidates(6, 2, 8),
                new RemoveCandidates(6, 8, 8)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, XCycles::xCyclesRule1);
    }

    @Test
    public void rule2() {
        var board = """
                8{19}4537{169}{126}{12}
                {79}23614{79}85
                6{17}5982{17}34
                {349}{346}{269}1{469}587{29}
                5{49}{12}7{49}83{12}6
                {179}8{1679}2{69}345{19}
                2{467}{167}859{16}{146}3
                {49}5{69}3712{469}8
                {139}{39}84265{19}7""".replace("\n", "");
        var expected = List.of(new SetValue(8, 0, 1));
        SudokuAssertions.assertLogicalSolution(expected, board, XCycles::xCyclesRule2);
    }

    @Test
    public void rule3Test1() {
        var board = """
                {158}762{35}{89}4{589}{1389}
                {58}941{35}7{2358}6{238}
                2{13}{1358}46{89}{1589}{589}7
                {589}6{258}371{2589}{24589}{2489}
                74{38}592{38}16
                {159}{123}{1235}684{2359}7{239}
                3{12}97{124}6{128}{248}5
                68{12}9{124}573{124}
                4578{12}36{29}{129}""".replace("\n", "");
        var expected = List.of(new RemoveCandidates(2, 2, 1));
        SudokuAssertions.assertLogicalSolution(expected, board, XCycles::xCyclesRule3);
    }

    @Test
    public void rule3Test2() {
        var board = """
                {2478}{23}{247}{357}1{357}96{28}
                {127}{1239}{1279}68{37}45{12}
                {18}569423{18}7
                {1247}{126}{12457}{157}{36}8{17}{137}9
                38{17}{17}94625
                9{16}{157}2{36}{157}{178}{1378}4
                673{18}2954{18}
                5{129}8476{12}{19}3
                {12}4{129}{138}5{13}{1278}{1789}6""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(5, 7, 1),
                new RemoveCandidates(8, 2, 1)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, XCycles::xCyclesRule3);
    }

    @Test
    public void rule3Test3() {
        var board = """
                {12456}{145}{1256}978{24}{2346}{236}
                {27}83{46}{46}159{27}
                {467}9{67}253{478}1{678}
                {29}74586{129}{23}{1239}
                86{29}134{279}5{279}
                {15}3{15}792684
                32{156}8{146}9{14}7{156}
                {145679}{145}8{46}{146}{57}3{246}{12569}
                {145679}{145}{15679}32{57}{1489}{46}{15689}""".replace("\n", "");
        var expected = List.of(new RemoveCandidates(4, 8, 2, 9));
        SudokuAssertions.assertLogicalSolution(expected, board, XCycles::xCyclesRule3);
    }

    @Test
    public void rule3Test4() {
        var board = """
                {23678}{23489}{24689}{168}5{69}{12378}{13467}{23467}
                {67}{48}12{48}39{67}5
                {2368}5{24689}{168}{489}7{1238}{1346}{2346}
                973421658
                165738429
                4{28}{28}965{37}{37}1
                {236}{12349}{2469}57{269}{123}8{2346}
                {268}{1289}73{89}45{169}{26}
                5{23489}{24689}{68}1{269}{237}{34679}{23467}""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(8, 1, 8),
                new RemoveCandidates(8, 2, 8)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, XCycles::xCyclesRule3);
    }
}