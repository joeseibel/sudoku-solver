package sudokusolver.javanostreams;

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

    public boolean isInSameUnit(UnsolvedCell other) {
        return row == other.row || column == other.column || block() == other.block();
    }

    @Override
    public String toString() {
        return "0";
    }
}
