package sudokusolver.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sudokusolver.java.logic.MultipleSolutionsException;
import sudokusolver.java.logic.NoSolutionsException;

class SudokuSolverTest {
    @Test
    public void testSolution() {
        var board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        var expected = """
                8 1 7 | 9 4 2 | 5 6 3
                2 3 4 | 6 1 5 | 7 8 9
                5 6 9 | 8 3 7 | 1 4 2
                ------+-------+------
                4 5 1 | 3 2 9 | 6 7 8
                6 2 3 | 7 8 1 | 4 9 5
                9 7 8 | 5 6 4 | 3 2 1
                ------+-------+------
                7 9 6 | 1 5 8 | 2 3 4
                1 8 2 | 4 7 3 | 9 5 6
                3 4 5 | 2 9 6 | 8 1 7""";
        Assertions.assertEquals(
                expected,
                Assertions.assertDoesNotThrow(() -> SudokuSolver.solve(parsedBoard)).toString()
        );
    }

    @Test
    public void testUnableToSolve() {
        var board = "004007830000050470720030695080700300649513728007008010470080060016040007005276100";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        var expected = """
                Unable to solve:
                0 0 4 | 0 0 7 | 8 3 0
                0 0 0 | 0 5 0 | 4 7 0
                7 2 0 | 0 3 0 | 6 9 5
                ------+-------+------
                0 8 0 | 7 0 0 | 3 0 0
                6 4 9 | 5 1 3 | 7 2 8
                0 0 7 | 0 0 8 | 0 1 0
                ------+-------+------
                4 7 0 | 0 8 0 | 0 6 0
                0 1 6 | 0 4 0 | 0 0 7
                0 0 5 | 2 7 6 | 1 0 0

                Simple String: 004007830000050470720030695080700300649513728007008010470080060016040007005276100

                With Candidates:
                {159}{569}4{169}{269}783{12}
                {139}{36}{38}{689}5{129}47{12}
                72{18}{148}3{14}695
                {125}8{12}7{269}{249}3{45}{469}
                649513728
                {235}{35}7{46}{269}8{59}1{469}
                47{23}{139}8{159}{259}6{39}
                {2389}16{39}4{59}{25}{58}7
                {38}{39}52761{48}{349}""";
        Assertions.assertEquals(
                expected,
                Assertions.assertThrows(
                        UnableToSolveException.class,
                        () -> SudokuSolver.solve(parsedBoard)
                ).getMessage()
        );
    }

    @Test
    public void testNoSolutions() {
        var board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        Assertions.assertThrows(NoSolutionsException.class, () -> SudokuSolver.solve(parsedBoard));
    }

    @Test
    public void testMultipleSolutions() {
        var board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        Assertions.assertThrows(MultipleSolutionsException.class, () -> SudokuSolver.solve(parsedBoard));
    }
}