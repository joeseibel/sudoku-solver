package sudokusolver.java;

public record SetValue(int row, int column, SudokuNumber value) implements BoardModification {
    public SetValue {
        Board.validateRowAndColumn(row, column);
    }

    public SetValue(UnsolvedCell cell, SudokuNumber value) {
        this(cell.row(), cell.column(), value);
        if (!cell.candidates().contains(value)) {
            throw new IllegalArgumentException(value + " is not a candidate for [" + row + ", " + column + "].");
        }
    }

    public SetValue(int row, int column, int value) {
        this(row, column, SudokuNumber.values()[value - 1]);
    }
}