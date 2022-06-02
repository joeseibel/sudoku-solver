package sudokusolver.javanostreams.logic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sudokusolver.javanostreams.BoardFactory;

class BruteForceTest {
    @Test
    public void testBruteForceSingleSolution() {
        var board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010";
        var expected = "817942563234615789569837142451329678623781495978564321796158234182473956345296817";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        var parsedExpected = BoardFactory.parseBoard(expected);
        Assertions.assertDoesNotThrow(() -> {
            var actual = BruteForce.bruteForce(parsedBoard);
            Assertions.assertEquals(parsedExpected, actual);
        });
    }

    @Test
    public void testBruteForceNoSolutions() {
        var board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        Assertions.assertThrows(NoSolutionsException.class, () -> BruteForce.bruteForce(parsedBoard));
    }

    @Test
    public void testBruteForceMultipleSolutions() {
        var board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        Assertions.assertThrows(MultipleSolutionsException.class, () -> BruteForce.bruteForce(parsedBoard));
    }

    @Test
    public void testBruteForceAlreadySolved() {
        var board = "817942563234615789569837142451329678623781495978564321796158234182473956345296817";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        var parsedExpected = BoardFactory.parseBoard(board);
        Assertions.assertDoesNotThrow(() -> {
            var actual = BruteForce.bruteForce(parsedBoard);
            Assertions.assertEquals(parsedExpected, actual);
        });
    }

    @Test
    public void testBruteForceInvalidSolution() {
        var board = "817942563234615789569837142451329678623781495978564321796158234182473956345296818";
        var parsedBoard = BoardFactory.parseOptionalBoard(board);
        Assertions.assertThrows(NoSolutionsException.class, () -> BruteForce.bruteForce(parsedBoard));
    }
}