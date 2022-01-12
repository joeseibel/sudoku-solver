package sudokusolver.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sudokusolver.java.logic.MultipleSolutionsException;
import sudokusolver.java.logic.NoSolutionsException;

class SudokuSolverTest {
    // TODO: Uncomment after implementing Naked Singles.
//    @Test
//    public void testSolveSolution() {
//        var board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010";
//        var expected = "817942563234615789569837142451329678623781495978564321796158234182473956345296817";
//        var parsedBoard = BoardFactory.parseOptionalBoard(board);
//        var parsedExpected = BoardFactory.parseBoard(expected);
//        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(parsedExpected, SudokuSolver.solve(parsedBoard)));
//    }

    // TODO: Uncomment after implementing Alternating Inference Chains.
//    @Test
//    public void testSolveUnableToSolve() {
//        var board = "004007830000050470720030695080700300649513728007008010470080060016040007005276100";
//        var expected = """
//                {159}{569}4{169}{269}783{12}
//                {139}{36}{38}{689}5{129}47{12}
//                72{18}{148}3{14}695
//                {125}8{12}7{269}{249}3{45}{469}
//                649513728
//                {235}{35}7{46}{269}8{59}1{469}
//                47{23}{139}8{159}{259}6{39}
//                {2389}16{39}4{59}{25}{58}7
//                {38}{39}52761{48}{349}""".replace("\n", "");
//        var parsedBoard = BoardFactory.parseOptionalBoard(board);
//        var actualException = Assertions.assertThrows(
//                UnableToSolveException.class,
//                () -> SudokuSolver.solve(parsedBoard)
//        );
//        var expectedException = new UnableToSolveException(BoardFactory.parseCellsWithCandidates(expected));
//        Assertions.assertEquals(expectedException.getMessage(), actualException.getMessage());
//    }
    
    @Test
    public void testSolveInvalidNoSolutions() {
        var board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        Assertions.assertThrows(NoSolutionsException.class, () -> SudokuSolver.solve(parsedBoard));
    }
    
    @Test
    public void testSolveInvalidMultipleSolutions() {
        var board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        Assertions.assertThrows(MultipleSolutionsException.class, () -> SudokuSolver.solve(parsedBoard));
    }
    
    @Test
    public void testUnableToSolveMessage() {
        var board = """
                {159}{569}4{169}{269}783{12}
                {139}{36}{38}{689}5{129}47{12}
                72{18}{148}3{14}695
                {125}8{12}7{269}{249}3{45}{469}
                649513728
                {235}{35}7{46}{269}8{59}1{469}
                47{23}{139}8{159}{259}6{39}
                {2389}16{39}4{59}{25}{58}7
                {38}{39}52761{48}{349}""".replace("\n", "");
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
        var parsedBoard = BoardFactory.parseCellsWithCandidates(board);
        var exception = new UnableToSolveException(parsedBoard);
        Assertions.assertEquals(expected, exception.getMessage());
    }
}