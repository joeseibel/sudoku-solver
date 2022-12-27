package sudokusolver.javanostreams.logic;

import org.junit.jupiter.api.Assertions;
import sudokusolver.javanostreams.Board;
import sudokusolver.javanostreams.BoardFactory;
import sudokusolver.javanostreams.BoardModification;
import sudokusolver.javanostreams.Cell;
import sudokusolver.javanostreams.RemoveCandidates;
import sudokusolver.javanostreams.SetValue;
import sudokusolver.javanostreams.SolvedCell;
import sudokusolver.javanostreams.SudokuNumber;

import java.util.ArrayList;
import java.util.Collections;
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
            if (cell instanceof SolvedCell solvedCell) {
                return Optional.of(solvedCell.value());
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
        var actual = new ArrayList<>(logicFunction.apply(board));
        Collections.sort(actual);
        for (var modification : actual) {
            var row = modification.row();
            var column = modification.column();
            var solution = bruteForceSolution.get(row, column);
            if (modification instanceof RemoveCandidates removeCandidates) {
                Assertions.assertFalse(
                        removeCandidates.candidates().contains(solution),
                        () -> "Cannot remove candidate " + solution + " from [" + row + ", " + column + ']'
                );
            } else if (modification instanceof SetValue setValue) {
                Assertions.assertEquals(
                        solution,
                        setValue.value(),
                        () -> "Cannot set value " + setValue.value() + " to [" + row + ", " + column +
                                "]. Solution is " + solution
                );
            }
        }
        Assertions.assertIterableEquals(expected, actual);
    }
}