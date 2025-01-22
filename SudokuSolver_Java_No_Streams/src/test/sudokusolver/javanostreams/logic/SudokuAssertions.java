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
        var actual = new ArrayList<>(logicFunction.apply(board));
        /*
         * Note: this comparator imposes orderings that are inconsistent with equals.
         *
         * Why am I using List.sort(Comparator) instead of Collections.sort(List) and having BoardModification implement
         * Comparable? In short, implementing Comparable for BoardModification would lead to BoardModification's natural
         * ordering being inconsistent with equals which is discouraged by Comparable. I want to sort BoardModifications
         * by the row and column indices only while ignoring other fields. However, I want equality to check all fields,
         * as that is useful in unit tests. While not a strict requirement, Comparable strongly recommends that natural
         * orderings be consistent with equals. Even though this recommendation also exists for Comparator, I am only
         * creating and using the custom Comparator here with the List.sort(Comparator) method, so its usage is limited
         * and doesn't apply generally to BoardModification.
         */
        actual.sort(Comparator.comparingInt(BoardModification::row).thenComparingInt(BoardModification::column));
        for (var modification : actual) {
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
        }
        Assertions.assertIterableEquals(expected, actual);
    }
}