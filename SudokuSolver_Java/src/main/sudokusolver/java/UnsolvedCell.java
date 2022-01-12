package sudokusolver.java;

import java.util.EnumSet;

public record UnsolvedCell(int row, int column, EnumSet<SudokuNumber> candidates) implements Cell {
    public UnsolvedCell {
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

    public UnsolvedCell(int row, int column) {
        this(row, column, EnumSet.allOf(SudokuNumber.class));
    }

    @Override
    public String toString() {
        return "0";
    }
}
