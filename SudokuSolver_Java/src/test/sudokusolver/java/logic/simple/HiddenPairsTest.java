package sudokusolver.java.logic.simple;

import org.junit.jupiter.api.Test;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.logic.SudokuAssertions;

import java.util.List;

class HiddenPairsTest {
    @Test
    public void test1() {
        var board = """
                {1258}{1238}{23}{129}{12359}{59}{4589}{2345679}{345679}
                9{1238}46{1235}7{58}{235}{35}
                {25}768{2359}41{2359}{359}
                3{246}97{2456}1{45}8{456}
                7{246}8{29}{24569}{569}3{4569}1
                {46}513{469}87{469}2
                {48}{3489}75{89}261{349}
                {16}{169}54{1679}32{79}8
                {12468}{1234689}{23}{19}{16789}{69}{459}{34579}{34579}""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(0, 7, 2, 3, 4, 5, 9),
                new RemoveCandidates(0, 8, 3, 4, 5, 9)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, HiddenPairs::hiddenPairs);
    }

    @Test
    public void test2() {
        var board = """
                72{56}4{19}8{1569}3{169}
                {569}8{356}{135}{129}{25}{1569}47
                4{359}1{35}768{59}2
                81{2456}739{56}{256}{46}
                {69}{379}{23467}851{3679}{269}{469}
                {59}{3579}{357}264{13579}8{19}
                2{57}968{57}413
                34{57}{15}{12}{257}{69}{69}8
                168943275""".replace("\n", "");
        var expected = List.of(
                new RemoveCandidates(3, 2, 5, 6),
                new RemoveCandidates(4, 2, 3, 6, 7),
                new RemoveCandidates(4, 6, 6, 9),
                new RemoveCandidates(5, 6, 1, 5, 9)
        );
        SudokuAssertions.assertLogicalSolution(expected, board, HiddenPairs::hiddenPairs);
    }
}