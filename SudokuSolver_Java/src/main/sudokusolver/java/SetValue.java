package sudokusolver.java;

public record SetValue(int row, int column, SudokuNumber value) implements BoardModification {
    public SetValue {
        if (row < 0 || row >= Board.UNIT_SIZE) {
            var message = "row is " + row + ", must be between 0 and " + (Board.UNIT_SIZE - 1) + '.';
            throw new IllegalArgumentException(message);
        }
        if (column < 0 || column >= Board.UNIT_SIZE) {
            var message = "column is " + column + ", must be between 0 and " + (Board.UNIT_SIZE - 1) + '.';
            throw new IllegalArgumentException(message);
        }
    }

    public SetValue(UnsolvedCell cell, SudokuNumber value) {
        this(cell.row(), cell.column(), value);
        if (!cell.candidates().contains(value)) {
            throw new IllegalArgumentException(value + " is not a candidate for [" + row + ", " + column + ']');
        }
    }

    public SetValue(int row, int column, int value) {
        this(row, column, SudokuNumber.values()[value - 1]);
    }
}
