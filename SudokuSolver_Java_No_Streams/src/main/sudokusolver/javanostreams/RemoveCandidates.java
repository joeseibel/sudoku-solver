package sudokusolver.javanostreams;

import java.util.EnumSet;

public record RemoveCandidates(int row, int column, EnumSet<SudokuNumber> candidates) implements BoardModification {
    public RemoveCandidates {
        Board.validateRowAndColumn(row, column);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates must not be empty.");
        }
    }

    public RemoveCandidates(UnsolvedCell cell, EnumSet<SudokuNumber> candidates) {
        this(cell.row(), cell.column(), candidates);
        for (var candidate : candidates) {
            if (!cell.candidates().contains(candidate)) {
                var message = candidate + " is not a candidate for [" + row + ", " + column + "].";
                throw new IllegalArgumentException(message);
            }
        }
    }

    public RemoveCandidates(int row, int column, int... candidates) {
        this(row, column, convertCandidates(candidates));
    }

    private static EnumSet<SudokuNumber> convertCandidates(int[] candidates) {
        var candidatesSet = EnumSet.noneOf(SudokuNumber.class);
        for (var candidate : candidates) {
            candidatesSet.add(SudokuNumber.values()[candidate - 1]);
        }
        return candidatesSet;
    }
}