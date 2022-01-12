package sudokusolver.java.logic.simple;

import org.junit.jupiter.api.Test;
import sudokusolver.java.SetValue;
import sudokusolver.java.logic.SudokuAssertions;

import java.util.List;

class NakedSinglesTest {
    @Test
    public void test() {
        var board = """
                {2367}{379}{29}1{3468}5{2389}{9}{289}
                14{259}{389}{38}{38}67{289}
                {3567}8{59}{3679}{36}24{59}{19}
                {2458}63{58}7{48}{89}1{489}
                9{57}{2458}{568}{124568}{1468}{78}{46}3
                {478}1{48}{368}9{3468}52{4678}
                {345}{359}72{1356}{136}{19}8{1469}
                {48}26{78}{18}{178}{179}35
                {358}{35}{158}4{13568}9{127}{6}{1267}""".replace("\n", "");
        var expected = List.of(
                new SetValue(0, 7, 9),
                new SetValue(8, 7, 6)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, NakedSingles::nakedSingles);
    }
}