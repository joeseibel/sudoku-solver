package sudokusolver.java;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public record RemoveCandidates(int row, int column, EnumSet<SudokuNumber> candidates) implements BoardModification {
    public RemoveCandidates {
        Board.validateRowAndColumn(row, column);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates must not be empty.");
        }
    }

    public RemoveCandidates(UnsolvedCell cell, EnumSet<SudokuNumber> candidates) {
        this(cell.row(), cell.column(), candidates);
        candidates.forEach(candidate -> {
            if (!cell.candidates().contains(candidate)) {
                var message = candidate + " is not a candidate for [" + row + ", " + column + "].";
                throw new IllegalArgumentException(message);
            }
        });
    }

    public RemoveCandidates(int row, int column, int... candidates) {
        this(row, column, Arrays.stream(candidates)
                .mapToObj(candidate -> SudokuNumber.values()[candidate - 1])
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class))));
    }
}
