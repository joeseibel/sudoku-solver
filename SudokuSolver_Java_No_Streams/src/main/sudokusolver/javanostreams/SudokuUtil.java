package sudokusolver.javanostreams;

public class SudokuUtil {
    public static void validateRowAndColumn(int row, int column) {
        if (row < 0 || row >= Board.UNIT_SIZE) {
            var message = "row is " + row + ", must be between 0 and " + (Board.UNIT_SIZE - 1) + '.';
            throw new IllegalArgumentException(message);
        }
        if (column < 0 || column >= Board.UNIT_SIZE) {
            var message = "column is " + column + ", must be between 0 and " + (Board.UNIT_SIZE - 1) + '.';
            throw new IllegalArgumentException(message);
        }
    }
}
