package sudokusolver.java.logic.simple;

import org.junit.jupiter.api.Test;
import sudokusolver.java.SetValue;
import sudokusolver.java.logic.SudokuAssertions;

import java.util.List;

class HiddenSinglesTest {
    @Test
    public void test() {
        var board = """
                2{459}{1569}{159}7{159}{159}38
                {458}{4589}{159}{123589}{159}6{1259}7{145}
                3{5789}{159}{12589}4{12589}6{1259}{15}
                {456}{3459}8{1569}2{1459}7{159}{135}
                1{23459}{2359}{5789}{59}{45789}{23589}{2589}6
                {56}{259}7{15689}3{1589}4{12589}{15}
                {57}{2357}4{12357}8{12357}{135}{156}9
                {578}6{235}4{159}{123579}{1358}{158}{1357}
                91{35}{357}6{357}{358}{458}2""".replace("\n", "");
        var expected = List.of(
                new SetValue(0, 1, 4),
                new SetValue(0, 2, 6),
                new SetValue(1, 3, 3),
                new SetValue(1, 8, 4),
                new SetValue(2, 1, 7),
                new SetValue(6, 7, 6),
                new SetValue(7, 0, 8),
                new SetValue(7, 8, 7),
                new SetValue(8, 7, 4)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, HiddenSingles::hiddenSingles);
    }
}