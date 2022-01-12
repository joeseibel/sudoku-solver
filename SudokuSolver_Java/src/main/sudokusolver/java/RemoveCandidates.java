package sudokusolver.java;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public record RemoveCandidates(int row, int column, EnumSet<SudokuNumber> candidates) implements BoardModification {
    public RemoveCandidates {
        if (row < 0 || row >= Board.UNIT_SIZE) {
            var message = "row is " + row + ", must be between 0 and " + (Board.UNIT_SIZE - 1) + '.';
            throw new IllegalArgumentException(message);
        }
        if (column < 0 || column >= Board.UNIT_SIZE) {
            var message = "column is " + column + ", must be between 0 and " + (Board.UNIT_SIZE - 1) + '.';
            throw new IllegalArgumentException(message);
        }
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates must not be empty.");
        }
    }

    public RemoveCandidates(UnsolvedCell cell, EnumSet<SudokuNumber> candidates) {
        this(cell.row(), cell.column(), candidates);
        candidates.forEach(candidate -> {
            if (!cell.candidates().contains(candidate)) {
                throw new IllegalArgumentException(candidate + " is not a candidate for [" + row + ", " + column + ']');
            }
        });
    }

    public RemoveCandidates(int row, int column, int... candidates) {
        this(row, column, Arrays.stream(candidates)
                .mapToObj(candidate -> SudokuNumber.values()[candidate - 1])
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SudokuNumber.class))));
    }
}
