package sudokusolver.java;

import java.util.EnumSet;

public record UnsolvedCell(int row, int column, EnumSet<SudokuNumber> candidates) implements Cell {
    public UnsolvedCell {
        SudokuUtil.validateRowAndColumn(row, column);
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
