package sudokusolver.java.logic;

import org.junit.jupiter.api.Assertions;
import sudokusolver.java.Board;
import sudokusolver.java.BoardFactory;
import sudokusolver.java.BoardModification;
import sudokusolver.java.Cell;
import sudokusolver.java.RemoveCandidates;
import sudokusolver.java.SetValue;
import sudokusolver.java.SolvedCell;
import sudokusolver.java.SudokuNumber;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SudokuAssertions {
    public static <T extends BoardModification> void assertLogicalSolution(
            List<T> expected,
            String withCandidates,
            Function<Board<Cell>, List<T>> logicFunction
    ) {
        assertLogicalSolution(expected, BoardFactory.parseCellsWithCandidates(withCandidates), logicFunction);
    }

    public static <T extends BoardModification> void assertLogicalSolution(
            List<T> expected,
            Board<Cell> board,
            Function<Board<Cell>, List<T>> logicFunction
    ) {
        var optionalBoard = board.mapCells(cell -> {
            if (cell instanceof SolvedCell(_, _, var value)) {
                return Optional.of(value);
            } else {
                return Optional.<SudokuNumber>empty();
            }
        });
        Board<SudokuNumber> bruteForceSolution;
        try {
            bruteForceSolution = BruteForce.bruteForce(optionalBoard);
        } catch (NoSolutionsException | MultipleSolutionsException e) {
            throw new RuntimeException(e);
        }
        var actual = logicFunction.apply(board).stream().sorted().toList();
        actual.forEach(modification -> {
            var row = modification.row();
            var column = modification.column();
            var solution = bruteForceSolution.get(row, column);
            switch (modification) {
                case RemoveCandidates(_, _, var candidates) -> Assertions.assertFalse(
                        candidates.contains(solution),
                        () -> "Cannot remove candidate " + solution + " from [" + row + ", " + column + ']'
                );
                case SetValue(_, _, var value) -> Assertions.assertEquals(
                        solution,
                        value,
                        () -> "Cannot set value " + value + " to [" + row + ", " + column + "]. Solution is "+ solution
                );
            }
        });
        Assertions.assertIterableEquals(expected, actual);
    }
}