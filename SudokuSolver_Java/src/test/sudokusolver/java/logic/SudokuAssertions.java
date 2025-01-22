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

import java.util.Comparator;
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
        /*
         * Why am I using sorted(Comparator) instead of sorted() and having BoardModification implement Comparable? In
         * short, implementing Comparable for BoardModification would lead to BoardModification's natural ordering being
         * inconsistent with equals which is discouraged by Comparable. I want to sort BoardModifications by the row and
         * column indices only while ignoring other fields. However, I want equality to check all fields, as that is
         * useful in unit tests. While not a strict requirement, Comparable strongly recommends that natural orderings
         * be consistent with equals. Even though this recommendation also exists for Comparator, I am only creating and
         * using the custom Comparator here with the sorted(Comparator) method, so its usage is limited and doesn't
         * apply generally to BoardModification.
         */
        var actual = logicFunction.apply(board)
                .stream()
                // Note: this comparator imposes orderings that are inconsistent with equals.
                .sorted(Comparator.comparingInt(BoardModification::row).thenComparingInt(BoardModification::column))
                .toList();
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
                        () -> "Cannot set value " + value + " to [" + row + ", " + column + "]. Solution is " + solution
                );
            }
        });
        Assertions.assertIterableEquals(expected, actual);
    }
}